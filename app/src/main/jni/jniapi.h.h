//
// Created by BRIAN DUVERNEAY on 7/18/16.
//

#ifndef JNIAPI_H
#define JNIAPI_H


extern "C" {

JNIEXPORT void JNICALL Java_tsaarni_nativeeglexample_NativeEglExample_nativeSetSurface(JNIEnv* jenv, jobject obj, jobject surface);
};

#endif // JNIAPI_H