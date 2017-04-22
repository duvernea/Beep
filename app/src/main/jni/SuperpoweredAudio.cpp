#include "SuperpoweredAudio.h"
#include "jniapi.h.h"
#include <SuperpoweredSimple.h>
#include <SuperpoweredDecoder.h>
#include <SuperpoweredTimeStretching.h>
#include <SuperpoweredAudioBuffers.h>
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <string>
#include <math.h>

static SuperpoweredAudio *myAudio = NULL;

static JavaVM *jvm;
static jclass activityClass;
static jobject activityObj;
static jmethodID playbackEndCallback;
static jmethodID recordLevelCallback;


// New function declarations
void setup(JNIEnv *javaEnvironment, jobject thisObj);

static void playerEventCallbackA(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event, void * __unused value) {
    //__android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "playerCallbackA");

    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
    	//SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
    };
    if (event == SuperpoweredAdvancedAudioPlayerEvent_EOF) {

        SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
        __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio EOF", "Native");

        playerA->pause(0, 0);
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, NULL);
        // callback to the android activity to update state
        playbackEndCallback = (env)->GetMethodID(activityClass, "playbackEndCallback", "()V");

        if (env != NULL && activityObj != NULL && playbackEndCallback != NULL) {
            (env)->CallVoidMethod(activityObj, playbackEndCallback);
        }
        jvm->DetachCurrentThread();
    }
}
static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples, int __unused samplerate) {
	return ((SuperpoweredAudio *)clientdata)->process(audioIO, (unsigned int)numberOfSamples);


}
#define MINFREQ 60.0f
#define MAXFREQ 20000.0f

static inline float floatToFrequency(float value) {
    if (value > 0.97f) return MAXFREQ;
    if (value < 0.03f) return MINFREQ;
    value = powf(10.0f, (value + ((0.4f - fabsf(value - 0.4f)) * 0.3f)) * log10f(MAXFREQ - MINFREQ)) + MINFREQ;
    return value < MAXFREQ ? value : MAXFREQ;
}

SuperpoweredAudio::SuperpoweredAudio(unsigned int samplerate, unsigned int buffersize):
        activeFx(0), volA(1.0f * headroom) {
    pthread_mutex_init(&mutex, NULL); // This will keep our player volumes and playback states in sync.
    stereoBuffer = (float *)memalign(16, (buffersize + 16) * sizeof(float) * 2);
    recordBuffer = (float *)memalign(16, (buffersize + 16) * sizeof(float) * 2);

    isRecording = false;

    playerA = new SuperpoweredAdvancedAudioPlayer(&playerA , playerEventCallbackA, samplerate, 2);
    //playerA->open(path, fileAoffset, fileAlength);

    playerA->syncMode = SuperpoweredAdvancedAudioPlayerSyncMode_None;
    reverse = false;

    const char *temp = "/data/data/xyz.peast.beep/files/temp.wav";
    recorder = new SuperpoweredRecorder(temp, samplerate);
    roll = new SuperpoweredRoll(samplerate);
    //filter->setResonantParameters(floatToFrequency(1.0f - .5f), 0.2f);
    // 3 Band EQ
    equalizer = new Superpowered3BandEQ(samplerate);
    equalizer->enable(true);
    equalizer->bands[0] = 1.0f;
    equalizer->bands[1] = 1.0f;
    equalizer->bands[1] = 1.0f;

    reverb = new SuperpoweredReverb(samplerate);
    reverb->setMix(0.5f);
    reverb->setRoomSize(0.8f);
    reverb->setWidth(0.8f);
    reverb->enable(false);

    filter = new SuperpoweredFilter(SuperpoweredFilter_Resonant_Lowpass, samplerate);
    filter->enable(false);

    flanger = new SuperpoweredFlanger(samplerate);
    flanger->clipperThresholdDb = -30.0f;
    flanger->clipperMaximumDb = -30.0f;
    flanger->setWet(.8f);
    flanger->setDepth(.99f);
    // Set this right for a nice sounding lfo. Limited to >= 60.0f and <= 240.0f. Read-write.
    flanger->bpm = 120.0f;
    // LFOBeats >= 0.25f and <= 64.0f
    flanger->setLFOBeats(3.0f);

    // TODO update
    echo = new SuperpoweredEcho(samplerate);
    echo->setMix(.7f);
    echo->bpm = 200.0f;
    echo->beats = .255f;
    echo->decay=0.40f;
    echo->enable(false);

    /**
    @brief Creates an audio I/O instance. Audio input and/or output immediately starts after calling this.

    @param samplerate The requested sample rate in Hz.
    @param buffersize The requested buffer size (number of samples).
    @param enableInput Enable audio input.
    @param enableOutput Enable audio output.
    @param callback The audio processing callback function to call periodically.
    @param clientdata A custom pointer the callback receives.
    @param inputStreamType OpenSL ES stream type, such as SL_ANDROID_RECORDING_PRESET_GENERIC. -1 means default. SLES/OpenSLES_AndroidConfiguration.h has them.
    @param outputStreamType OpenSL ES stream type, such as SL_ANDROID_STREAM_MEDIA or SL_ANDROID_STREAM_VOICE. -1 means default. SLES/OpenSLES_AndroidConfiguration.h has them.
    @param latencySamples How many samples to have in the internal fifo buffer minimum. Works only when both input and output are enabled. Might help if you have many dropouts.
    */
    audioSystem = new SuperpoweredAndroidAudioIO(samplerate, buffersize, true, true, audioProcessing, this, SL_ANDROID_RECORDING_PRESET_GENERIC, SL_ANDROID_STREAM_MEDIA, buffersize * 2);
    // audioSystem->stop();
}

SuperpoweredAudio::~SuperpoweredAudio() {
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "Deconstructor run..");

    delete audioSystem;
    delete playerA;
    free(stereoBuffer);
    pthread_mutex_destroy(&mutex);
}
void SuperpoweredAudio::onFileChange(const char *path, int fileOffset, int fileLength) {
    // __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudioOnFileChange", path);

    playerA->open(path);
    playerA->cachePosition(0, 255);
    playerA->cachePosition(playerA->durationMs, 255);
}

void SuperpoweredAudio::onPlayerPause() {
    playerA->pause();
}
void SuperpoweredAudio::setPitchShift(int pitchShift) {
    playerA->setPitchShift (pitchShift);
}
void SuperpoweredAudio::setTempo(double tempo) {
    playerA->setTempo (tempo, true);
}
void SuperpoweredAudio::setReverse(bool reverse) {
    __android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "set reverse %d\n", reverse);
    myAudio->reverse = reverse;
}
void SuperpoweredAudio::setReverb(bool enableReverb) {
    __android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "set reverb %d\n", enableReverb);
    myAudio->enableReverb = enableReverb;
    reverb->enable(enableReverb);
}
void SuperpoweredAudio::setRobot(bool enableRobot) {
    __android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "set robot %d\n", enableRobot);
    flanger->enable(enableRobot);
    // TODO
    // echo
    // flanger
    // pitch shift
}
void SuperpoweredAudio::turnFxOff() {
    flanger->enable(false);
    reverb->enable(false);
    echo->enable(false);
    equalizer->enable(false);
}

void SuperpoweredAudio::onPlayPause() {

    if (!isRecording) {
        if (myAudio->reverse) {
            playerA->setReverse(reverse, 5);
            playerA->setPosition((double) playerA->durationMs, false, false);
            playerA->play(0);
        } else {
            playerA->setReverse(reverse, 5);
            playerA->setPosition(0, false, false);
            playerA->seek(0);
            playerA->play(0);
        }
    }
}
void SuperpoweredAudio::onFxSelect(int value) {
	__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "FXSEL %i", value);
	activeFx = (unsigned char)value;
}

void SuperpoweredAudio::onFxOff() {
    filter->enable(false);
    roll->enable(false);
    flanger->enable(false);
}
void SuperpoweredAudio::setEcho(bool echoSetting) {
    echo->enable(echoSetting);
}
void SuperpoweredAudio::setBass(float bass) {
    equalizer->bands[0] = bass;
}
void SuperpoweredAudio::setTreble(float treble) {
    equalizer->bands[2] = treble;
}

void SuperpoweredAudio::shutdownAudio() {
    audioSystem->stop();
}
void SuperpoweredAudio::startupAudio() {
    audioSystem->start();
}

void SuperpoweredAudio::onFxValue(int ivalue) {
    float value = float(ivalue) * 0.01f;
    switch (activeFx) {
        case 1:
            filter->setResonantParameters(floatToFrequency(1.0f - value), 0.2f);
            filter->enable(true);
            flanger->enable(false);
            roll->enable(false);
            break;
        case 2:
            if (value > 0.8f) roll->beats = 0.0625f;
            else if (value > 0.6f) roll->beats = 0.125f;
            else if (value > 0.4f) roll->beats = 0.25f;
            else if (value > 0.2f) roll->beats = 0.5f;
            else roll->beats = 1.0f;
            roll->enable(true);
            filter->enable(false);
            flanger->enable(false);
            break;
        default:
            flanger->setWet(value);
            flanger->enable(true);
            filter->enable(false);
            roll->enable(false);
    };
}
char* SuperpoweredAudio::createReverseWav(const char *path) {
    int fileExtension = 4;
    int reverseFileSuffix = strlen("_reverse");

    char *pathWithExtension;
    pathWithExtension = (char *) calloc(strlen(path) + fileExtension, sizeof(char));
    strcpy(pathWithExtension, path);
    strcat(pathWithExtension, ".wav");

    char *reversePathWithExtension;
    reversePathWithExtension = (char *) calloc(strlen(path) + reverseFileSuffix + fileExtension, sizeof(char));
    strcpy(reversePathWithExtension, path);
    strcat(reversePathWithExtension, "_reverse");
    strcat(reversePathWithExtension, ".wav");

    // Open the input file
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio reverse original", pathWithExtension);
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio reverse edited", reversePathWithExtension);

    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();
    const char *openError = decoder->open(pathWithExtension, false, 0, 0);
    if (openError) {
        __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", openError);
        delete decoder;
        return NULL;
    }
    int64_t durationSamples = decoder->durationSamples;
    int64_t samplePosition = decoder->samplePosition;
    uint samplesPerFrame = decoder->samplesPerFrame;

    // Create the output WAV file.
    FILE *fd = createWAV(reversePathWithExtension, decoder->samplerate, 2);

    // Create a buffer for the 16-bit integer samples.
    short int *intBuffer = (short int *)malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);
    // Create a buffer for 16-bit, reversed samples
    short int *intBufferReverse = (short int *)malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);

    int64_t startSample = durationSamples - samplesPerFrame;

    int iteration = 0;
    // processing
    while (true) {
        // Decode one frame. samplesDecoded will be overwritten with the actual decoded number of samples

        unsigned int samplesDecoded = decoder->samplesPerFrame;
        if (startSample <= 0) {
            break;
        }

        decoder->seekTo(startSample, true);

        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) break;
        if (samplesDecoded < 1) break;

        // Reverse audio - Stereo interleaved samples*2 for left and right channel
        for (unsigned int i=0; i<samplesDecoded*2; i=i+2) {
            intBufferReverse[i] = intBuffer[samplesDecoded*2-i-2];
            intBufferReverse[i+1] = intBuffer[samplesDecoded*2-i-1];
        }
        // Write the audio to disk
        fwrite(intBufferReverse, 1, samplesDecoded * 4, fd);

        startSample = startSample-samplesPerFrame;

    };
    // Cleanup
    closeWAV(fd);
    delete decoder;
    free(intBuffer);
    free(intBufferReverse);

    return reversePathWithExtension;

}

void SuperpoweredAudio::createWav(const char *path, BeepFx beepFx) {

    // Note: passed in path does not have '.wav' appended
    int fileExtension = 4;
    int editFileSuffix = strlen("_edit");

    char *pathWithExtension;
    if (beepFx.reverse) {
        pathWithExtension = createReverseWav(path);
    } else {
        pathWithExtension = (char *) calloc(strlen(path) + fileExtension, sizeof(char));
        strcpy(pathWithExtension, path);
        strcat(pathWithExtension, ".wav");
    }

    char *editPathWithExtension;
    editPathWithExtension = (char *) calloc(strlen(path) + editFileSuffix + fileExtension, sizeof(char));
    strcpy(editPathWithExtension, path);
    strcat(editPathWithExtension, "_edit");
    strcat(editPathWithExtension, ".wav");

    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();
    // TODO - use actual file name
    const char *openError = decoder->open(pathWithExtension, false, 0, 0);
    if (openError) {
        __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", openError);
        delete decoder;
        return;
    }
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio pathoriginal", pathWithExtension);
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio pathedited", editPathWithExtension);


    // Create the output WAV file.
    FILE *fd = createWAV(editPathWithExtension, decoder->samplerate, 2);

    /* Need to use variable size buffer chains for time stretching */
    SuperpoweredTimeStretching *timeStretch = new SuperpoweredTimeStretching(decoder->samplerate);
    float rate = (float) beepFx.tempo;
    char rate_float[10];
    sprintf(rate_float, "%f", rate);
    __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio rate float", rate_float);

    timeStretch->setRateAndPitchShift(rate, beepFx.pitchShift);
    // This buffer list will receive the time-stretched samples.
    SuperpoweredAudiopointerList *outputBuffers = new SuperpoweredAudiopointerList(8, 16);
    // Create a buffer for the 16-bit integer samples.
    short int *intBuffer = (short int *)malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);

    // processing
    while (true) {
        // Decode one frame. samplesDecoded will be overwritten with the actual decoded number of samples

        unsigned int samplesDecoded = decoder->samplesPerFrame;
        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) break;
        if (samplesDecoded < 1) break;

        // Create an input buffer for the time stretcher
        SuperpoweredAudiobufferlistElement inputBuffer;
        inputBuffer.samplePosition = decoder->samplePosition;
        inputBuffer.startSample = 0;
        inputBuffer.samplesUsed = 0;
        inputBuffer.endSample = samplesDecoded;
        inputBuffer.buffers[0] = SuperpoweredAudiobufferPool::getBuffer(samplesDecoded * 8 + 64);
        inputBuffer.buffers[1] = inputBuffer.buffers[2] = inputBuffer.buffers[3] = NULL;

        // Convert the decoded PCM samples from 16-bit integer to 32-bit floating point.
        SuperpoweredShortIntToFloat(intBuffer, (float *) inputBuffer.buffers[0], samplesDecoded);

        // Time stretching
        timeStretch->process(&inputBuffer, outputBuffers);

        // Do we have some output?
        if (outputBuffers->makeSlice(0, outputBuffers->sampleLength)) {
            while (true) {
                // Iterate on every output slice.
                // Get pointer to the output samples.
                int numSamples = 0;
                float *timeStretchedAudio = (float *) outputBuffers->nextSliceItem(&numSamples);
                if (!timeStretchedAudio) break;

                // Convert the time stretched PCM samples from 32-bit floating point to 16-bit integer.
                SuperpoweredFloatToShortInt(timeStretchedAudio, intBuffer, numSamples);
                // Write the audio to disk
                fwrite(intBuffer, 1, numSamples * 4, fd);

            };
            // Clear the output buffer list.
            outputBuffers->clear();
        };
    };
    // Cleanup
    closeWAV(fd);
    delete decoder;
    delete timeStretch;
    delete outputBuffers;
    free(intBuffer);
    if (beepFx.reverse) {
        remove(pathWithExtension);
    }
}

bool SuperpoweredAudio::process(short int *output, unsigned int numberOfSamples) {
    bool silence = false;

    if (isRecording) {
        // short int -32,768 to 32,767
        short int *localAudioPointer = output;
        float RMS =0;
        //SuperpoweredFloatToShortInt(recordBuffer, output, numberOfSamples);
        for (int i=0; i<numberOfSamples; i+=1) {
            //__android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "test int = %d", i);
            //__android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "float = %f", *localAudioPointer);
            RMS += (*localAudioPointer) * (*localAudioPointer);
            localAudioPointer+=1;
        }
        RMS = sqrtl(RMS / numberOfSamples);

        //char array[40];
        //sprintf(array, "%f", RMS);
        //__android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio Buffer RMS value", array );

        JNIEnv *env;
        jvm->AttachCurrentThread(&env, NULL);
        // callback to the android activity to update state
        recordLevelCallback = (env)->GetMethodID(activityClass, "onBufferCallback", "(F)V");

        if (env != NULL && activityObj != NULL && recordLevelCallback != NULL) {
            (env)->CallVoidMethod(activityObj, recordLevelCallback, (jfloat) RMS);
        }
        jvm->DetachCurrentThread();

        //__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "process record start");
        //__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "process.. isRecording");

        SuperpoweredShortIntToFloat(output, recordBuffer, (unsigned int) numberOfSamples);
//        float *localAudioPointer = recordBuffer;
//        float RMS =0;
//        //SuperpoweredFloatToShortInt(recordBuffer, output, numberOfSamples);
//        for (int i=0; i<numberOfSamples; i+=1) {
//            //__android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "test int = %d", i);
//           // __android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "float = %f", *localAudioPointer);
//            RMS += (*localAudioPointer) * (*localAudioPointer);
//            localAudioPointer+=1;
//        }

        recorder->process(recordBuffer, NULL, numberOfSamples);
        silence = !playerA->process(stereoBuffer, false, numberOfSamples);
        //__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "process end");
    }
    else {

        bool masterIsA = true;
        double masterBpm = playerA->currentBpm;
        double msElapsedSinceLastBeatA = playerA->msElapsedSinceLastBeat; // When playerB needs it, playerA has already stepped this value, so save it now.

        silence = !playerA->process(stereoBuffer, false, numberOfSamples, volA, 0.0f, -1);

        if (!silence) {
            equalizer->process(stereoBuffer, stereoBuffer, numberOfSamples);
            flanger->process(stereoBuffer, stereoBuffer, numberOfSamples);
            reverb->process(stereoBuffer, stereoBuffer, numberOfSamples);
            echo->process(stereoBuffer, stereoBuffer, numberOfSamples);
        }
        // The stereoBuffer is ready now, let's put the finished audio into the requested buffers.
        if (!silence) {
            SuperpoweredFloatToShortInt(stereoBuffer, output, numberOfSamples);
        }
    }
    return !silence;
}
void SuperpoweredAudio::setRecordFileName(std::string filename) {
    recordFileName = filename;
}

void SuperpoweredAudio::toggleRecord(bool record) {
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "toggleRecord called..");

//    JNIEnv * env;
//    bool attached = false;
//    switch (jvm->GetEnv((void**)&env, JNI_VERSION_1_6))
//    {
//        case JNI_OK:
//            break;
//        case JNI_EDETACHED:
//            if (jvm->AttachCurrentThread(&env, NULL)!=0)
//            {
//                throw std::runtime_error("Could not attach current thread");
//            }
//            attached = true;
//            break;
//        case JNI_EVERSION:
//            throw std::runtime_error("Invalid java version");
//    }
//    static const char *a = env->GetStringUTFChars(filePath, JNI_FALSE);
////    static const char *b = env->GetStringUTFChars(filename, JNI_FALSE);
//    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudioMember a", a);
//    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudioMember b", b);
//
//
//    //env->ReleaseStringUTFChars(filePath, a);
//    if (attached)
//    {
//        jvm->DetachCurrentThread();
//    }

    //pthread_mutex_lock(&mutex);
    isRecording = record;
    if (isRecording) {
        __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "toggleRecord startRecord");

        //playerA->open(musicpath, musicOffset, musicLength);
        //playerA->play(false);
        //const char *path = "/data/data/xyz.peast.beep/files/testing.wav";
        const char *b = recordFileName.c_str();
        __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", b);
        recorder->start(b);
    }
    else {
        __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "toggleRecord stopRecord");

        playerA->pause();
        recorder->stop();
    }
    //pthread_mutex_unlock(&mutex);
}

/***************************  Native Function Calls ***************************************/

//SuperpoweredAudio Constructor
extern "C" JNIEXPORT
void Java_xyz_peast_beep_MainActivity_SuperpoweredAudio(JNIEnv *javaEnvironment, jobject thisObj, jint samplerate, jint buffersize) {
    //const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    //__android_log_write(ANDROID_LOG_ERROR, "SuperpoweredInitialPath", path);
    myAudio = new SuperpoweredAudio((unsigned int) samplerate, (unsigned int) buffersize);
    //javaEnvironment->ReleaseStringUTFChars(apkPath, path);
}
//onPlayPause - Main Activity
extern "C" JNIEXPORT
void Java_xyz_peast_beep_MainActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj) {
    myAudio->onPlayPause();
}
//onPlayPause - Board Activity
extern "C" JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj, jstring filepath, jboolean play, jint size) {
    myAudio->onPlayPause();
}
//onPlayerPause - MainActivity
extern "C" JNIEXPORT
void Java_xyz_peast_beep_MainActivity_onPlayerPause(JNIEnv * __unused javaEnvironment, jobject __unused obj) {
    myAudio->onPlayerPause();
}
//onPlayerPause - BoardActivity
extern "C" JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_onPlayerPause(JNIEnv * __unused javaEnvironment, jobject __unused obj) {
    myAudio->onPlayerPause();
}



//onFxSelect
extern "C" JNIEXPORT
void Java_xyz_peast_beep_MainActivity_onFxSelect(JNIEnv * __unused javaEnvironment, jobject __unused obj, jint value) {
	myAudio->onFxSelect(value);
}
//onFxOff
extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onFxOff(JNIEnv * __unused javaEnvironment, jobject __unused obj) {
	myAudio->onFxOff();
}
//onFxValue
extern "C"JNIEXPORT
void Java_xyz_peast_beep_MainActivity_onFxValue(JNIEnv * __unused javaEnvironment, jobject __unused obj, jint value) {
	myAudio->onFxValue(value);
}
//onFileChange
extern "C"JNIEXPORT
void Java_xyz_peast_beep_MainActivity_onFileChange(JNIEnv * __unused javaEnvironment, jobject, jstring apkPath, jint fileOffset, jint fileLength ) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    myAudio->onFileChange(path, fileOffset, fileLength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
    //__android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", path);
}
//onFileChange - Board Activity
extern "C"JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_onFileChange(JNIEnv * __unused javaEnvironment, jobject, jstring apkPath, jint fileOffset, jint fileLength ) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    myAudio->onFileChange(path, fileOffset, fileLength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
    //__android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", path);
}
//Superpowered Audio Constructor
//consolidate with helper function or remove?
extern "C"JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_SuperpoweredAudio(JNIEnv *javaEnvironment, jobject thisObj, jint samplerate, jint buffersize) {
    //const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    //const char *recordFileName = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    //__android_log_write(ANDROID_LOG_ERROR, "SuperpoweredInitialPath", path);
    myAudio = new SuperpoweredAudio((unsigned int)samplerate, (unsigned int)buffersize);
    //javaEnvironment->ReleaseStringUTFChars(apkPath, path);

}
//onPlayPause
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj) {
    myAudio->onPlayPause();
}
//onFileChange
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_onFileChange(JNIEnv * __unused javaEnvironment, jobject, jstring apkPath, jint fileOffset, jint fileLength ) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    myAudio->onFileChange(path, fileOffset, fileLength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", path);

}
// setPitchShift
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setPitchShift(JNIEnv * __unused javaEnvironment, jobject __unused obj, jint pitchShift) {
    myAudio->setPitchShift(pitchShift);
}
// setTempo
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setTempo(JNIEnv * __unused javaEnvironment, jobject __unused obj, jdouble tempo) {
    myAudio->setTempo(tempo);
}
// setReverse
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setReverse(JNIEnv * __unused javaEnvironment, jobject __unused obj, jboolean reverse) {
    myAudio->setReverse(reverse);
}
// setReverb
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setReverb(JNIEnv * __unused javaEnvironment, jobject __unused obj, jboolean enableReverb) {
    myAudio->setReverb(enableReverb);
}
// set echo
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setEcho(JNIEnv * __unused javaEnvironment, jobject __unused obj, jboolean echo) {
    myAudio->setEcho(echo);
}
// set robot
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setRobot(JNIEnv * __unused javaEnvironment, jobject __unused obj, jboolean robot) {
    myAudio->setRobot(robot);
}
// set bass
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setBass(JNIEnv * __unused javaEnvironment, jobject __unused obj, jfloat bass) {
    myAudio->setBass(bass);
}
// set treble
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setTreble(JNIEnv * __unused javaEnvironment, jobject __unused obj, jfloat treble) {
    myAudio->setTreble(treble);
}
//createWAV
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_createWav(JNIEnv * javaEnvironment, jobject, jstring filePath, jobject jBeepFx) {
    const char *path = javaEnvironment->GetStringUTFChars(filePath, JNI_FALSE);

    BeepFx beepFx;

    // Get Field IDs
    jclass cls = javaEnvironment->GetObjectClass(jBeepFx);

    jfieldID fidTreble = javaEnvironment->GetFieldID(cls, "mTreble", "F");
    jfieldID fidBass = javaEnvironment->GetFieldID(cls, "mBass", "F");
    jfieldID fidTempo = javaEnvironment->GetFieldID(cls, "mTempo", "D");
    jfieldID fidPitchShift = javaEnvironment->GetFieldID(cls, "mPitchShift", "I");
    jfieldID fidEcho = javaEnvironment->GetFieldID(cls, "mEcho", "Z");
    jfieldID fidReverse = javaEnvironment->GetFieldID(cls, "mReverse", "Z");
    jfieldID fidReverb = javaEnvironment->GetFieldID(cls, "mReverb", "Z");
    jfieldID fidRobot = javaEnvironment->GetFieldID(cls, "mRobot", "Z");

    // Get Field Values
    beepFx.treble = (float) javaEnvironment->GetFloatField(jBeepFx, fidTreble);
    beepFx.bass = (float) javaEnvironment->GetFloatField(jBeepFx, fidBass);
    beepFx.pitchShift = (int) javaEnvironment->GetIntField(jBeepFx, fidPitchShift);
    beepFx.tempo = (double) javaEnvironment->GetDoubleField(jBeepFx, fidTempo);
    beepFx.echo = (bool) javaEnvironment->GetBooleanField(jBeepFx, fidEcho);
    beepFx.reverse = (bool) javaEnvironment->GetBooleanField(jBeepFx, fidReverse);
    beepFx.reverb = (bool) javaEnvironment->GetBooleanField(jBeepFx, fidReverb);
    beepFx.robot = (bool) javaEnvironment->GetBooleanField(jBeepFx, fidRobot);

    char c[10]; //becuase double is 8 bytes in GCC compiler but take 10 for safety
    sprintf(c , "%lf" , beepFx.tempo);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio createWAV tempo", c);

    char d[10]; //becuase double is 8 bytes in GCC compiler but take 10 for safety
    sprintf(d , "%i" , beepFx.pitchShift);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio createWAV pitchshift", d);


    myAudio->createWav(path, beepFx);
    // myAudio->createReverseWav(path);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio createWAV path", path);

    javaEnvironment->ReleaseStringUTFChars(filePath, path);
}

//toggleRecord
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_toggleRecord(JNIEnv * __unused javaEnvironment, jobject __unused obj, jboolean record) {
    myAudio->toggleRecord(record);
}
//setRecordPath
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setRecordPath(JNIEnv * javaEnvironment, jobject __unused obj, jstring path) {

    const char *tempChars = javaEnvironment->GetStringUTFChars(path,NULL);
    std::string tempString;
    tempString=tempChars;
    myAudio->setRecordFileName(tempString);
    javaEnvironment->ReleaseStringUTFChars(path,tempChars);

//    const char *t = javaEnvironment->GetStringUTFChars(filePath, JNI_FALSE);
//    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudioGlobal", t);
//    javaEnvironment->ReleaseStringUTFChars(path, filepath);
}

extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_setupAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    setup(javaEnvironment, thisObj);
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_MainActivity_setupAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    setup(javaEnvironment, thisObj);
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_setupAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    setup(javaEnvironment, thisObj);
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_MainActivity_shutdownAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    //setup(javaEnvironment, thisObj);
    myAudio->shutdownAudio();
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_shutdownAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    //setup(javaEnvironment, thisObj);
    myAudio->shutdownAudio();
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_shutdownAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    //setup(javaEnvironment, thisObj);
    myAudio->shutdownAudio();
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_startupAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    //setup(javaEnvironment, thisObj);
    myAudio->startupAudio();
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_startupAudio(JNIEnv *javaEnvironment, jobject thisObj) {
    //setup(javaEnvironment, thisObj);
    myAudio->startupAudio();
}
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_turnFxOff(JNIEnv *javaEnvironment, jobject thisObj) {
    myAudio->turnFxOff();
}

/***************************  Helper Functions ***************************************/

// Set the static Activity Class to be the current activity
void setup(JNIEnv *javaEnvironment, jobject thisObj) {
    javaEnvironment->GetJavaVM(&jvm);

//*************     Print Activity Class Name     *********************************
    jclass cls = javaEnvironment->GetObjectClass(thisObj);
    jmethodID mid = javaEnvironment->GetMethodID(cls, "getClass", "()Ljava/lang/Class;");
    jobject clsObj = javaEnvironment->CallObjectMethod(thisObj, mid);
    cls = javaEnvironment->GetObjectClass(clsObj);
    mid = javaEnvironment->GetMethodID(cls, "getName", "()Ljava/lang/String;");
    jstring strObj = (jstring)javaEnvironment->CallObjectMethod(clsObj, mid);
    const char* str = javaEnvironment->GetStringUTFChars(strObj, NULL);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", str);
    javaEnvironment->ReleaseStringUTFChars(strObj, str);
 //*********************************************************************************/

    jclass thisClass = (javaEnvironment)->GetObjectClass(thisObj);

    if (activityClass == NULL) {
        activityClass = (jclass) javaEnvironment->NewGlobalRef(thisClass);
        activityObj = javaEnvironment->NewGlobalRef(thisObj);
    }
    else if (activityClass != thisClass){
        activityClass = (jclass) javaEnvironment->NewGlobalRef(thisClass);
        activityObj = javaEnvironment->NewGlobalRef(thisObj);
    }
    (javaEnvironment)->DeleteLocalRef(thisClass);

}
