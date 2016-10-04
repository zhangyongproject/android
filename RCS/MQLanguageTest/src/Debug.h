//---------------------------------------------------
// Log
//---------------------------------------------------
#ifdef ANDROID

#include <android/log.h>

#define LOG_TAG "JNI_DEBUG"

#define LOGI(...)
#define LOGW(...)
#define LOGE(...)

#endif

#ifdef IOS

#define LOGI(...)
#define LOGW(...)
#define LOGE(...)

#endif


#ifdef WIN32

#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#endif