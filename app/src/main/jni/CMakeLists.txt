cmake_minimum_required(VERSION 3.4.1)

set(
	PATH_TO_SUPERPOWERED
	CACHE STRING ""
)

message(${ANDROID_ABI})

file(GLOB CPP_FILES "*.cpp")

add_library(
	SuperpoweredAudio
	SHARED
	${CPP_FILES}
	/Users/duverneay/AndroidStudioProjects/Capstone/beep/SuperpoweredSDK/Superpowered/AndroidIO/SuperpoweredAndroidAudioIO.cpp
)

include_directories(src/main/jni)
include_directories(/Users/duverneay/AndroidStudioProjects/Capstone/beep/SuperpoweredSDK/Superpowered/)

target_link_libraries(
    SuperpoweredAudio
    log
    android
    OpenSLES
    /Users/duverneay/AndroidStudioProjects/Capstone/beep/SuperpoweredSDK/Superpowered/libSuperpoweredAndroid${ANDROID_ABI}.a
)