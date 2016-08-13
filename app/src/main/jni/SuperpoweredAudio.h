#ifndef Header_SuperpoweredAudio
#define Header_SuperpoweredAudio

#include <math.h>
#include <pthread.h>

#include "SuperpoweredAudio.h"
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredFilter.h>
#include <SuperpoweredRoll.h>
#include <SuperpoweredFlanger.h>
#include <SuperpoweredRecorder.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include <jni.h>

#define HEADROOM_DECIBEL 3.0f
static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025f);

class SuperpoweredAudio {
public:

	SuperpoweredAudio(unsigned int samplerate, unsigned int buffersize);
	~SuperpoweredAudio();

	bool process(short int *output, unsigned int numberOfSamples);
	void onPlayPause(const char *path, bool play, int size);
	void onPlayerPause();
	void onFxSelect(int value);
	void onFxOff();
	void onFxValue(int value);
	void onFileChange(const char *path, int fileOffset, int fileLength);
	void toggleRecord(bool record);
	void setFileName(jstring name);

private:
    pthread_mutex_t mutex;
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA;
    SuperpoweredRoll *roll;
    SuperpoweredFilter *filter;
    SuperpoweredFlanger *flanger;
	SuperpoweredRecorder *recorder;
    float *stereoBuffer;
	float *recordBuffer;
    unsigned char activeFx;
    float volA;
	bool isRecording;
	jstring filename;
};

#endif
