cmake_minimum_required(VERSION 3.4.1)

set(
	PATH_TO_SUPERPOWERED
	${CMAKE_CURRENT_SOURCE_DIR}/../../../../SuperpoweredSDK/Superpowered)

message(${ANDROID_ABI})

file(GLOB CPP_FILES "*.cpp")

add_library(
	SuperpoweredAudio
	SHARED
	${CPP_FILES}
	${PATH_TO_SUPERPOWERED}/AndroidIO/SuperpoweredAndroidAudioIO.cpp
)

include_directories(src/main/jni)
include_directories(${PATH_TO_SUPERPOWERED})

target_link_libraries(
    SuperpoweredAudio
    log
    android
    OpenSLES
    ${PATH_TO_SUPERPOWERED}/libSuperpoweredAndroid${ANDROID_ABI}.a
)