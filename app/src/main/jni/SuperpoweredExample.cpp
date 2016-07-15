#include "SuperpoweredExample.h"
#include <SuperpoweredSimple.h>
#include <jni.h>
#include <stdio.h>
#include <android/log.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_AndroidConfiguration.h>
#include <android/log.h>
#include <string>

static SuperpoweredExample *example = NULL;
static jmethodID midIntegerInit;
static JavaVM *jvm;
static jclass activityClass;
static jobject activityObj;
static jmethodID testCallback;

static void playerEventCallbackA(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event, void * __unused value) {
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "playerCallbackA");

    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
    	SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
        //playerA->setBpm(126.0f);
        //playerA->setFirstBeatMs(353);
        //playerA->setPosition(playerA->firstBeatMs, false, false);
        //playerA->setPitchShift(-6);

        //playerA->setReverse(false, 0);
        //playerA->setTempo(.5f, true);
       // __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "playerCallbackA running..");

    };
    if (event == SuperpoweredAdvancedAudioPlayerEvent_EOF) {
        __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "playerCallbackA EOF");
        SuperpoweredAdvancedAudioPlayer *playerA = *((SuperpoweredAdvancedAudioPlayer **)clientData);
        playerA->pause(0, 0);
        //(environment)->CallVoidMethod(activityObj, testCallback);
        JNIEnv *env;
        jvm->AttachCurrentThread(&env, NULL);


        //int status = jvm->GetEnv((void **)&env, JNI_VERSION_1_6);
        if (env != NULL && activityObj != NULL && testCallback != NULL) {
            (env)->CallVoidMethod(activityObj, testCallback);
        }
        jvm->DetachCurrentThread();


        //if(status < 0) {
            //LogNativeToAndroidExt("callback_handler: failed to get JNI environment, assuming native thread");
            //status = gJVM->AttachCurrentThread(&env, NULL);
            //if(status < 0)

            //g_vm->AttachCurrentThread((void **) &g_env, NULL) != 0)
            //            (environment)->CallVoidMethod(activityObj, testCallback);
            //if (env )
            //assert (rs == JNI_OK);
            // Use the env pointer...
        //}
    }
}

static void playerEventCallbackB(void *clientData, SuperpoweredAdvancedAudioPlayerEvent event, void * __unused value) {
    if (event == SuperpoweredAdvancedAudioPlayerEvent_LoadSuccess) {
    	SuperpoweredAdvancedAudioPlayer *playerB = *((SuperpoweredAdvancedAudioPlayer **)clientData);
        //playerB->setBpm(123.0f);
        //playerB->setFirstBeatMs(40);
        //playerB->setPosition(playerB->firstBeatMs, false, false);
    };
}

static bool audioProcessing(void *clientdata, short int *audioIO, int numberOfSamples, int __unused samplerate) {
	return ((SuperpoweredExample *)clientdata)->process(audioIO, (unsigned int)numberOfSamples);
}
#define MINFREQ 60.0f
#define MAXFREQ 20000.0f

static inline float floatToFrequency(float value) {
    if (value > 0.97f) return MAXFREQ;
    if (value < 0.03f) return MINFREQ;
    value = powf(10.0f, (value + ((0.4f - fabsf(value - 0.4f)) * 0.3f)) * log10f(MAXFREQ - MINFREQ)) + MINFREQ;
    return value < MAXFREQ ? value : MAXFREQ;
}

SuperpoweredExample::SuperpoweredExample(unsigned int samplerate, unsigned int buffersize, const char *path, int fileAoffset, int fileAlength, int fileBoffset, int fileBlength) : activeFx(0), crossValue(0.0f), volB(0.0f), volA(1.0f * headroom) {
    pthread_mutex_init(&mutex, NULL); // This will keep our player volumes and playback states in sync.
    stereoBuffer = (float *)memalign(16, (buffersize + 16) * sizeof(float) * 2);
    recordBuffer = (float *)memalign(16, (buffersize + 16) * sizeof(float) * 2);

    isRecording = false;


    playerA = new SuperpoweredAdvancedAudioPlayer(&playerA , playerEventCallbackA, samplerate, 0);
    playerA->open(path, fileAoffset, fileAlength);
    playerB = new SuperpoweredAdvancedAudioPlayer(&playerB, playerEventCallbackB, samplerate, 0);
    playerB->open(path, fileBoffset, fileBlength);

    playerA->syncMode = playerB->syncMode = SuperpoweredAdvancedAudioPlayerSyncMode_None;

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

SuperpoweredExample::~SuperpoweredExample() {
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "Deconstructor run..");

    delete audioSystem;
    delete playerA;
    //delete playerB;
    free(stereoBuffer);
    pthread_mutex_destroy(&mutex);
}
void SuperpoweredExample::onFileChange(const char *path, int fileOffset, int fileLength) {
    pthread_mutex_lock(&mutex);
    playerA->open(path);
    playerA->cachePosition(0, 255);
    //playerA->setBpm(bpm);
    //playerA->setFirstBeatMs(beatStart);
    //playerA->setPosition(0, false, false);
    //playerA->open(path, fileOffset, fileLength);
    //__android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "onFileChange run");

    //double a = .90;
    //playerA->seek(a);
    //playerA->setPosition(10, 1, 0);
    //playerA->seek(50);
    pthread_mutex_unlock(&mutex);


}

void SuperpoweredExample::onPlayPause(const char *path, bool play, int size) {
    //__android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "onPlayPause called");
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
//        __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "onPlayPause PAUSE");
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

void SuperpoweredExample::onCrossfader(int value) {
    pthread_mutex_lock(&mutex);
    crossValue = float(value) * 0.01f;
    if (crossValue < 0.01f) {
        volA = 1.0f * headroom;
        volB = 0.0f;
    } else if (crossValue > 0.99f) {
        volA = 0.0f;
        volB = 1.0f * headroom;
    } else { // constant power curve
        volA = cosf(float(M_PI_2) * crossValue) * headroom;
        volB = cosf(float(M_PI_2) * (1.0f - crossValue)) * headroom;
    };
    pthread_mutex_unlock(&mutex);
}

void SuperpoweredExample::onFxSelect(int value) {
	__android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "FXSEL %i", value);
	activeFx = (unsigned char)value;
}

void SuperpoweredExample::onFxOff() {
    filter->enable(false);
    roll->enable(false);
    flanger->enable(false);
}





void SuperpoweredExample::onFxValue(int ivalue) {
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

bool SuperpoweredExample::process(short int *output, unsigned int numberOfSamples) {
    //const char* numSamples =  (std::to_string(numberOfSamples)).c_str();
    //__android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", numSamples);

    pthread_mutex_lock(&mutex);
    bool silence = false;

    if (isRecording) {
        __android_log_print(ANDROID_LOG_VERBOSE, "SuperpoweredExample", "process.. isRecording");

        SuperpoweredShortIntToFloat(output, recordBuffer, numberOfSamples, NULL);
        //SuperpoweredFloatToShortInt(recordBuffer, output, numberOfSamples);
        recorder->process(recordBuffer, NULL, numberOfSamples);
        silence = !playerA->process(stereoBuffer, false, numberOfSamples, volB);
    }
    else {

        bool masterIsA = (crossValue <= 0.5f);
        double masterBpm = masterIsA ? playerA->currentBpm : playerB->currentBpm;
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
    pthread_mutex_unlock(&mutex);
    return !silence;
}
void SuperpoweredExample::toggleRecord(bool record) {
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "toggleRecord called..");

    pthread_mutex_lock(&mutex);
    isRecording = record;
    if (isRecording) {
        __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "toggleRecord startRecord");

        //playerA->open(musicpath, musicOffset, musicLength);
        //playerA->play(false);
        const char *path = "/data/data/xyz.peast.beep/files/temp.wav";
        recorder->start(path);
    }
    else {
        __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", "toggleRecord stopRecord");

        playerA->pause();
        recorder->stop();

    }

    pthread_mutex_unlock(&mutex);
}





extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_SuperpoweredExample(JNIEnv *javaEnvironment, jobject __unused obj, jint samplerate, jint buffersize, jstring apkPath, jint fileAoffset, jint fileAlength, jint fileBoffset, jint fileBlength) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredInitialPath", path);

    example = new SuperpoweredExample((unsigned int)samplerate, (unsigned int)buffersize, path, fileAoffset, fileAlength, fileBoffset, fileBlength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);

}

extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj, jstring filepath, jboolean play, jint size) {
    const char *path = javaEnvironment->GetStringUTFChars(filepath, JNI_FALSE);

    example->onPlayPause(path, play, size);
    javaEnvironment->ReleaseStringUTFChars(filepath, path);

}

extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onCrossfader(JNIEnv * __unused javaEnvironment, jobject __unused obj, jint value) {
	example->onCrossfader(value);
}

extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onFxSelect(JNIEnv * __unused javaEnvironment, jobject __unused obj, jint value) {
	example->onFxSelect(value);
}

extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onFxOff(JNIEnv * __unused javaEnvironment, jobject __unused obj) {
	example->onFxOff();
}

extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onFxValue(JNIEnv * __unused javaEnvironment, jobject __unused obj, jint value) {
	example->onFxValue(value);
}
extern "C" JNIEXPORT void Java_xyz_peast_beep_MainActivity_onFileChange(JNIEnv * __unused javaEnvironment, jobject, jstring apkPath, jint fileOffset, jint fileLength ) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    example->onFileChange(path, fileOffset, fileLength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", path);

}

extern "C" JNIEXPORT void Java_xyz_peast_beep_RecordActivity_SuperpoweredExample(JNIEnv *javaEnvironment, jobject thisObj, jint samplerate, jint buffersize, jstring apkPath, jint fileAoffset, jint fileAlength, jint fileBoffset, jint fileBlength) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredInitialPath", path);

    example = new SuperpoweredExample((unsigned int)samplerate, (unsigned int)buffersize, path, fileAoffset, fileAlength, fileBoffset, fileBlength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);

    //environment = javaEnvironment;
    if (NULL == activityClass) {
        printf("Find java.lang.Integer\n");
        // FindClass returns a local reference
        jclass thisClass = (javaEnvironment)->GetObjectClass(thisObj);
        //jclass classIntegerLocal = (*env)->FindClass(env, "java/lang/Integer");
        // Create a global reference from the local reference
        //activityClass = (javaEnvironment)->NewGlobalRef(thisClass);
        // No longer need the local reference, free it!
        (javaEnvironment)->DeleteLocalRef(thisClass);
        jclass cls = javaEnvironment->GetObjectClass(thisObj);
        activityClass = (jclass) javaEnvironment->NewGlobalRef(cls);
        activityObj = javaEnvironment->NewGlobalRef(thisObj);

    }
    if (NULL == testCallback) {
        //printf("Get Method ID for java.lang.Integer's constructor\n");
        //midIntegerInit = (javaEnvironment)->GetMethodID(classInteger, "<init>", "(I)V");
        testCallback = (javaEnvironment)->GetMethodID(activityClass, "testCallback", "()V");

    }
    //(environment)->CallVoidMethod(activityObj, testCallback);

}
extern "C" JNIEXPORT void Java_xyz_peast_beep_RecordActivity_onPlayPause(JNIEnv * __unused javaEnvironment, jobject __unused obj, jstring filepath, jboolean play, jint size) {
    const char *path = javaEnvironment->GetStringUTFChars(filepath, JNI_FALSE);

    example->onPlayPause(path, play, size);
    javaEnvironment->ReleaseStringUTFChars(filepath, path);

}
extern "C" JNIEXPORT void Java_xyz_peast_beep_RecordActivity_onFileChange(JNIEnv * __unused javaEnvironment, jobject, jstring apkPath, jint fileOffset, jint fileLength ) {
    const char *path = javaEnvironment->GetStringUTFChars(apkPath, JNI_FALSE);
    example->onFileChange(path, fileOffset, fileLength);
    javaEnvironment->ReleaseStringUTFChars(apkPath, path);
    __android_log_write(ANDROID_LOG_ERROR, "SuperpoweredExample", path);

}

extern "C" JNIEXPORT void Java_xyz_peast_beep_RecordActivity_toggleRecord(JNIEnv * __unused javaEnvironment, jobject __unused obj, jboolean record) {
    example->toggleRecord(record);
}
extern "C" JNIEXPORT void Java_xyz_peast_beep_RecordActivity_setUp(JNIEnv *javaEnvironment, jobject thisObj) {

    //environment = javaEnvironment->NewGlobalRef(javaEnvironment);
    javaEnvironment->GetJavaVM(&jvm);
    //assert (rs == JNI_OK);
    if (NULL == activityClass) {
        printf("Find java.lang.Integer\n");
        // FindClass returns a local reference
        jclass thisClass = (javaEnvironment)->GetObjectClass(thisObj);
        //jclass classIntegerLocal = (*env)->FindClass(env, "java/lang/Integer");
        // Create a global reference from the local reference
        //activityClass = (javaEnvironment)->NewGlobalRef(thisClass);
        // No longer need the local reference, free it!
        (javaEnvironment)->DeleteLocalRef(thisClass);
        jclass cls = javaEnvironment->GetObjectClass(thisObj);
        activityClass = (jclass) javaEnvironment->NewGlobalRef(cls);
        activityObj = javaEnvironment->NewGlobalRef(thisObj);

    }
    if (NULL == testCallback) {
        //printf("Get Method ID for java.lang.Integer's constructor\n");
        //midIntegerInit = (javaEnvironment)->GetMethodID(classInteger, "<init>", "(I)V");
        testCallback = (javaEnvironment)->GetMethodID(activityClass, "testCallback", "()V");

    }
    //(environment)->CallVoidMethod(activityObj, testCallback);

}





