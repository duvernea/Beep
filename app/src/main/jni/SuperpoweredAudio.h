#ifndef Header_SuperpoweredAudio
#define Header_SuperpoweredAudio

#include <math.h>
#include <pthread.h>

#include "SuperpoweredAudio.h"
#include "BeepFx.h"
#include <SuperpoweredAdvancedAudioPlayer.h>
#include <SuperpoweredFilter.h>
#include <SuperpoweredRoll.h>
#include <SuperpoweredFlanger.h>
#include <SuperpoweredRecorder.h>
#include <SuperpoweredEcho.h>
#include <Superpowered3BandEQ.h>
#include <SuperpoweredReverb.h>
#include <AndroidIO/SuperpoweredAndroidAudioIO.h>
#include <jni.h>
#include <iosfwd>
#include <string>

#define HEADROOM_DECIBEL 3.0f
static const float headroom = powf(10.0f, -HEADROOM_DECIBEL * 0.025f);

class SuperpoweredAudio {
public:

	SuperpoweredAudio(unsigned int samplerate, unsigned int buffersize);
	~SuperpoweredAudio();

	bool process(short int *output, unsigned int numberOfSamples);
	void onPlayPause(const char *path, bool play, int size);
	void onPlayerPause();
	void setPitchShift(int pitchShift);
	void setReverse(bool reverse);
	void onFxSelect(int value);
	void onFxOff();
	void onFxValue(int value);
	void onFileChange(const char *path, int fileOffset, int fileLength);
	void toggleRecord(bool record);
	void setRecordFileName(std::string filename);
	void shutdownAudio();
	void startupAudio();
	void createWav(const char *path, BeepFx beepFx);
	void setEcho(bool echoSetting);
	void setBass(float bass);
	void setTreble(float treble);
	void setReverb(bool reverbSetting);
	void setRobot(bool robotSetting);

private:
    pthread_mutex_t mutex;
    SuperpoweredAndroidAudioIO *audioSystem;
    SuperpoweredAdvancedAudioPlayer *playerA;
    SuperpoweredRoll *roll;
	SuperpoweredEcho *echo;
    SuperpoweredFilter *filter;
    SuperpoweredFlanger *flanger;
	SuperpoweredRecorder *recorder;
	Superpowered3BandEQ *equalizer;
	SuperpoweredReverb *reverb;
    float *stereoBuffer;
	float *recordBuffer;
    unsigned char activeFx;
    float volA;
	bool isRecording;
	bool reverse;
	bool enableReverb;
	std::string recordFileName;
};

#endif
