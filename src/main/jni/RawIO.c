/*
 * Copyright (c) 2006 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

#include <jni.h>
#include "vavix_io_RawIO.h"
#include <windows.h>
#include <winioctl.h>


/*
 * Raw Device Access.
 *
 * @author	<a href=mailto:vavivavi@yahoo.co.jp>nsano</a>
 * @version	0.00	060108	nsano	initial version <br>
 */

//-----------------------------------------------------------------------------

/**
 * アーカイブのハンドルを取得します。
 * @return NativeGcaArchive#instance
 */
/** */
#define getDriveHandle(env,obj) getIntField((env),(obj),"handle")

/**
 * bytesPerSector を取得します。
 * @return NativeGcaArchive#instance
 */
#define getBytesPerSector(env,obj) getIntField((env),(obj),"bytesPerSector")

/**
 * int 型のフィールド値を取得します。
 */
static jint getIntField(JNIEnv *env, jobject obj, char *name) {
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID field = (*env)->GetFieldID(env, class, name, "I");
    return (jint)(*env)->GetLongField(env, obj, field);
}

/**
 * 例外を投げます。
 * @param exception "java/lang/Exception"
 */
static void throwExceptionWithStringMessage(JNIEnv *env, char *exception, char *_message) {

    jclass class = (*env)->FindClass(env, exception);
    (*env)->ThrowNew(env, class, _message);
}

/*
 * Class:     vavix_io_RawIO
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_vavix_io_RawIO_open(JNIEnv *env, jobject obj, jstring deviceName) {
 
    jboolean jdummy;
    char *_deviceName = (*env)->GetStringUTFChars(env, deviceName, &jdummy);
    HANDLE handle = CreateFile(_deviceName,
                               GENERIC_READ,
	                       FILE_SHARE_READ,
	                       NULL,
                               OPEN_EXISTING,
 		               0,
		               NULL); 
    if (handle == INVALID_HANDLE_VALUE) {
        throwExceptionWithStringMessage(env, "java/io/IOException", _deviceName);
        (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
        return;
    }

    DWORD dummy;
    DISK_GEOMETRY diskGeometory;
    if (!DeviceIoControl(handle,
                         IOCTL_DISK_GET_DRIVE_GEOMETRY,
                         NULL, 0, 
	                 &diskGeometory, sizeof(diskGeometory),
	 	         &dummy,
	                 (LPOVERLAPPED) NULL)) {
        throwExceptionWithStringMessage(env, "java/io/IOException", "IOCTL_DISK_GET_DRIVE_GEOMETRY");
        (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
        return;
    }

//fprintf(stderr, "handle: %08x\n", (unsigned int) handle);
    jclass class = (*env)->GetObjectClass(env, obj);
    jfieldID field = (*env)->GetFieldID(env, class, "handle", "I");
    (*env)->SetIntField(env, obj, field, (jint) handle);

    field = (*env)->GetFieldID(env, class, "bytesPerSector", "I");
    (*env)->SetIntField(env, obj, field, (jint) diskGeometory.BytesPerSector);

//fprintf(stderr, "%s: type: %d\n", _deviceName, diskGeometory.MediaType);
//fprintf(stderr, " Cylinders: %ld:%ld\n", diskGeometory.Cylinders.HighPart, diskGeometory.Cylinders.LowPart);
//fprintf(stderr, " TracksPerCylinder: %d\n", (unsigned int) diskGeometory.TracksPerCylinder);
//fprintf(stderr, " SectorsPerTrack: %d\n", (unsigned int) diskGeometory.SectorsPerTrack);
//fprintf(stderr, " BytesPerSector: %d\n", (unsigned int) diskGeometory.BytesPerSector);
//fflush(stderr);
    (*env)->ReleaseStringUTFChars(env, deviceName, _deviceName);
}

/*
 * Class:     vavix_io_RawIO
 * Method:    read
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_vavix_io_RawIO_read(JNIEnv *env, jobject obj, jint sectorNo, jbyteArray buffer) {

    HANDLE handle = (HANDLE) getDriveHandle(env, obj);
    int bytesPerSector = getBytesPerSector(env, obj);

    LONG distanceLow = ((long long) sectorNo * bytesPerSector) % 0x100000000;
    LONG distanceHigh = ((long long) sectorNo * bytesPerSector) / 0x100000000;
//fprintf(stderr, "distance: %ld:%ld\n", distanceHigh, distanceLow);
//fflush(stderr);
    if (SetFilePointer(handle, distanceLow, &distanceHigh, FILE_BEGIN) == -1) {
        throwExceptionWithStringMessage(env, "java/io/IOException", "SetFilePointer");
        return;
    }

    jboolean jdummy;
    jbyte *buf = (*env)->GetByteArrayElements(env, buffer, &jdummy);

    DWORD dummy;
    if (!ReadFile(handle, buf, bytesPerSector, &dummy, NULL)) {
        (*env)->ReleaseByteArrayElements(env, buffer, buf, 0);
        throwExceptionWithStringMessage(env, "java/io/IOException", "ReadFile");
        return;
    }

    (*env)->SetByteArrayRegion(env, buffer, 0, bytesPerSector, buf);
    (*env)->ReleaseByteArrayElements(env, buffer, buf, 0);
}

/**
 * Class:     vavix_io_RawIO
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_vavix_io_RawIO_close(JNIEnv *env, jobject obj) {

    HANDLE handle = (HANDLE) getDriveHandle(env, obj);

    if (!CloseHandle(handle)) {
        throwExceptionWithStringMessage(env, "java/io/IOException", "CloseHandle");
        return;
    }
}

/* */
