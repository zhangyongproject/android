#include <string>
#include "com_cyjh_input_InputEventStub.h"
#include "Debug.h"
#include "types.h"
#include "Input.h"

extern "C" JNIEXPORT void JNICALL Java_com_cyjh_input_InputEventStub_SetProtoType(JNIEnv *env, jobject, jint iProtoType)
{
	return InputDevice::GetInstance().SetProtoType(iProtoType);
}

extern "C" JNIEXPORT void JNICALL Java_com_cyjh_input_InputEventStub_WriteInitData(JNIEnv *env, jobject, jint iX, jint iY)
{
	return InputDevice::GetInstance().WriteInitData(iX, iY);
}

extern "C" JNIEXPORT void JNICALL Java_com_cyjh_input_InputEventStub_TouchDownEvent(JNIEnv *env, jobject, jint iX, jint iY, jint iFingerId)
{
	InputDevice::GetInstance().WritePointerInput(0, iX, iY, iFingerId, 0);
}

extern "C" JNIEXPORT void JNICALL Java_com_cyjh_input_InputEventStub_TouchMoveEvent(JNIEnv *env, jobject, jint iX, jint iY, jint iFingerId, jint iTime)
{
	InputDevice::GetInstance().WritePointerInput(1, iX, iY, iFingerId, iTime);
}

extern "C" JNIEXPORT void JNICALL Java_com_cyjh_input_InputEventStub_TouchUpEvent(JNIEnv *env, jobject, jint iFingerId)
{
	InputDevice::GetInstance().WritePointerInput(2, 0, 0, iFingerId, 0);
}
