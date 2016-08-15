#include "SuperpoweredAudio.h"
#include "jniapi.h.h"
#include <SuperpoweredSimple.h>
#include <SuperpoweredDecoder.h>
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <string>

static SuperpoweredAudio *myAudio = NULL;

static JavaVM *jvm;
static jclass activityClass;
static jobject activityObj;
static jmethodID playbackEndCallback;


// New function declarations
void setup(JNIEnv *javaEnvironment, jobject thisObj);

static void playerEventCallbackA(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event, void * __unused value) {
    //__android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "playerCallbackA");

    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
    	//SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
    };
    if (event == SuperpoweredAdvancedAudioPlayerEvent_EOF) {
        SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
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

    playerA = new SuperpoweredAdvancedAudioPlayer(&playerA , playerEventCallbackA, samplerate, 0);
    //playerA->open(path, fileAoffset, fileAlength);
    //playerB = new SuperpoweredAdvancedAudioPlayer(&playerB, playerEventCallbackB, samplerate, 0);
    //playerB->open(path, fileBoffset, fileBlength);

    playerA->syncMode = SuperpoweredAdvancedAudioPlayerSyncMode_None;

    const char *temp = "/data/data/xyz.peast.beep/files/temp.wav";
    recorder = new SuperpoweredRecorder(temp, samplerate);

    roll = new SuperpoweredRoll(samplerate);
    //filter->setResonantParameters(floatToFrequency(1.0f - .5f), 0.2f);

    filter = new SuperpoweredFilter(SuperpoweredFilter_Resonant_Lowpass, samplerate);

    filter->enable(false);
    flanger = new SuperpoweredFlanger(samplerate);

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
    pthread_mutex_lock(&mutex);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudioOnFileChange", path);

    playerA->open(path);
    playerA->cachePosition(0, 255);
    pthread_mutex_unlock(&mutex);
}

void SuperpoweredAudio::onPlayerPause() {
    playerA->pause();
}

void SuperpoweredAudio::onPlayPause(const char *path, bool play, int size) {
    //__android_log_write(ANDROID_LOG_ERROR, "SuperpoweredPATH", path);

    pthread_mutex_lock(&mutex);

    //audioSystem->start();
    //const char *path = "/data/data/xyz.peast.beep/files/d5925c56-c611-49ae-91bd-bc1d25ff6b56.mp3";
    //playerA->open(path, 0, size);
    if (!isRecording) {
    playerA->setPosition(0, false, false);
    //playerA->seek(0);
    playerA->play(0);
//    if (!play) {
//        __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredAudio", "onPlayPause PAUSE");
//
//        //playerA->seek(0);
//        //playerA->play(0);
//        playerA->pause();
//        playerB->pause();
//    } else {
//        bool masterIsA = (crossValue <= 0.5f);
//        playerA->play(!masterIsA);
//        playerB->play(masterIsA);
//    };
    }
    pthread_mutex_unlock(&mutex);
    //__android_log_write(ANDROID_LOG_ERROR, "Superpowered", "onPlayPause mutex unlocked");
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
void SuperpoweredAudio::createWav() {
    const char * temppath = "/data/data/xyz.peast.beep/files/createwavtest.wav";

    const char *recordedFile = recordFileName.c_str();
    const char *testFile = "data/data/xyz.peast.beep/files/04505f9a-2ab1-496b-acd3-6f26d9466892.wav";



    SuperpoweredDecoder *decoder = new SuperpoweredDecoder();
    const char *openError = decoder->open(testFile, false, 0, 0);
    if (openError) {
        __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", openError);
        delete decoder;
        return;
    }
    FILE *fd = createWAV(temppath, decoder->samplerate, 2);


    // int buffer for values from decoder
    short int *intBuffer = (short int *)malloc(decoder->samplesPerFrame * 2 * sizeof(short int) + 16384);

    // processing
    while (true) {
        unsigned int samplesDecoded = decoder->samplesPerFrame;

        if (decoder->decode(intBuffer, &samplesDecoded) == SUPERPOWEREDDECODER_ERROR) break;
        if (samplesDecoded < 1) break;

        fwrite(intBuffer, 1, samplesDecoded * 4, fd);
    }
    int numsamples = 44100*3;

    closeWAV(fd);
    delete decoder;
    free(intBuffer);
}

bool SuperpoweredAudio::process(short int *output, unsigned int numberOfSamples) {
    //const char* numSamples =  (std::to_string(numberOfSamples)).c_str();
    pthread_mutex_lock(&mutex);
    bool silence = false;

    if (isRecording) {
        //__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "process record start");

        //__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "process.. isRecording");

        SuperpoweredShortIntToFloat(output, recordBuffer, numberOfSamples, NULL);
        float *localAudioPointer = recordBuffer;
        //SuperpoweredFloatToShortInt(recordBuffer, output, numberOfSamples);
//        for (int i=0; i<numberOfSamples; i+=2) {
//            //__android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "test int = %d", i);
//            __android_log_print(ANDROID_LOG_DEBUG, "SuperpoweredAudio", "float = %f", *localAudioPointer);
//            localAudioPointer+=2;
//        }
        recorder->process(recordBuffer, NULL, numberOfSamples);
        silence = !playerA->process(stereoBuffer, false, numberOfSamples);
        //__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "process end");


    }
    else {

        bool masterIsA = true;
        double masterBpm = playerA->currentBpm;
        double msElapsedSinceLastBeatA = playerA->msElapsedSinceLastBeat; // When playerB needs it, playerA has already stepped this value, so save it now.

        silence = !playerA->process(stereoBuffer, false, numberOfSamples, volA, 0.0f,
                                    -1);

//        if (roll->process(silence ? NULL : stereoBuffer, stereoBuffer, numberOfSamples) &&
//            silence)
//            silence = false;
//        filter->process(stereoBuffer, stereoBuffer, numberOfSamples);
//
//        if (!silence) {
//            filter->process(stereoBuffer, stereoBuffer, numberOfSamples);
//            flanger->process(stereoBuffer, stereoBuffer, numberOfSamples);
//        };

        // The stereoBuffer is ready now, let's put the finished audio into the requested buffers.
        if (!silence) SuperpoweredFloatToShortInt(stereoBuffer, output, numberOfSamples);
    }
//    if (silence == 1) {
//        __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "silence true");
//    }
//    else {
//        __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredAudio", "silence false");
//    }

    pthread_mutex_unlock(&mutex);

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

    pthread_mutex_lock(&mutex);
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
    pthread_mutex_unlock(&mutex);
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
void Java_xyz_peast_beep_MainActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj, jstring filepath, jboolean play, jint size) {
    const char *path = javaEnvironment->GetStringUTFChars(filepath, JNI_FALSE);
    myAudio->onPlayPause(path, play, size);
    javaEnvironment->ReleaseStringUTFChars(filepath, path);
}
//onPlayPause - Board Activity
extern "C" JNIEXPORT
void Java_xyz_peast_beep_BoardActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj, jstring filepath, jboolean play, jint size) {
    const char *path = javaEnvironment->GetStringUTFChars(filepath, JNI_FALSE);
    myAudio->onPlayPause(path, play, size);
    javaEnvironment->ReleaseStringUTFChars(filepath, path);
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
void Java_xyz_peast_beep_RecordActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj, jstring filepath, jboolean play, jint size) {
    const char *path = javaEnvironment->GetStringUTFChars(filepath, JNI_FALSE);

    myAudio->onPlayPause(path, play, size);
    javaEnvironment->ReleaseStringUTFChars(filepath, path);

}
//onFileChange
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_onFileChange(JNIEnv * __unused javaEnvironment, jobject, jstring apkPath, jint fileOffset, jint fileLength ) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    myAudio->onFileChange(path, fileOffset, fileLength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
    __android_log_write(ANDROID_LOG_DEBUG, "SuperpoweredAudio", path);

}
//createWAV
extern "C" JNIEXPORT
void Java_xyz_peast_beep_RecordActivity_createWav(JNIEnv * javaEnvironment, jobject) {
    myAudio->createWav();
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
