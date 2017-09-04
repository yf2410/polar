#include <jni.h>
#include "AdblockInvoke.h"
#include "NavigateQueryInvoke.h"
#include "StringConvertor.h"
#include <sstream>
#include "Util.h"

extern "C" {

	JNIEXPORT void JNICALL
	Java_com_polar_browser_jni_NativeManager_initAdBlock(JNIEnv* env, jobject thiz, jstring data, jstring data2);

	JNIEXPORT jboolean JNICALL
	Java_com_polar_browser_jni_NativeManager_isNeedBlock(JNIEnv* env, jobject thiz, jstring mainUrl,
			jstring mainHost, jstring url, jstring urlHost);

	JNIEXPORT jstring JNICALL
	Java_com_polar_browser_jni_NativeManager_getAdCss(JNIEnv* env, jobject thiz, jstring url,
			jstring urlHost);

	JNIEXPORT void JNICALL
	Java_com_polar_browser_jni_NativeManager_initNativeQueryData(JNIEnv* env, jobject thiz, jint type, jstring data);

	JNIEXPORT jstring JNICALL
	Java_com_polar_browser_jni_NativeManager_addressInput(JNIEnv* env, jobject thiz, jbyteArray data);

	// UINT nType, LPCTSTR lpszTitle, LPCTSTR lpszUrl, UINT nNavType, time_t tAccTime
	JNIEXPORT void JNICALL
	Java_com_polar_browser_jni_NativeManager_addItem(JNIEnv* env, jobject thiz, jint type,
			jbyteArray title, jstring url, jint navType, jlong time);
	
	JNIEXPORT void Java_com_polar_browser_jni_NativeManager_init(JNIEnv *env, jclass jcl,
															 jobject context_object);

}

JNIEXPORT void JNICALL
Java_com_polar_browser_jni_NativeManager_initAdBlock(JNIEnv* env, jobject thiz, jstring data, jstring data2) {
	LPCTSTR pData = (TCHAR*)(env)->GetStringChars(data, 0);
	LPCTSTR pData2 = (TCHAR*)(env)->GetStringChars(data2, 0);

	AdblockInitialize(pData, pData2, 0);

	env->ReleaseStringChars(data, (jchar *)pData);
	env->ReleaseStringChars(data2, (jchar *)pData2);
}

JNIEXPORT jboolean JNICALL
Java_com_polar_browser_jni_NativeManager_isNeedBlock(JNIEnv* env, jobject thiz, jstring mainUrl,
		jstring mainHost, jstring url, jstring urlHost) {
	LPCTSTR lpszMainUrl = (TCHAR*)env->GetStringChars(mainUrl, 0);
	LPCTSTR lpszMainHost = (TCHAR*)env->GetStringChars(mainHost, 0);
	LPCTSTR lpszUrl = (TCHAR*)env->GetStringChars(url, 0);
	LPCTSTR lpszUrlHost = (TCHAR*)env->GetStringChars(urlHost, 0);

	bool isNeedBlock = AdblockMatchUrlByRaw(lpszMainUrl, lpszMainHost, lpszUrl, lpszUrlHost);

	env->ReleaseStringChars(mainUrl, (jchar *)lpszMainUrl);
	env->ReleaseStringChars(mainHost, (jchar *)lpszMainHost);
	env->ReleaseStringChars(url, (jchar *)lpszUrl);
	env->ReleaseStringChars(urlHost, (jchar *)lpszUrlHost);

	return (jboolean)isNeedBlock;
}

JNIEXPORT jstring JNICALL
Java_com_polar_browser_jni_NativeManager_getAdCss(JNIEnv* env, jobject thiz, jstring url,
		jstring urlHost) {

	LPCTSTR lpszUrl = (TCHAR*)env->GetStringChars(url, 0);
	LPCTSTR lpszUrlHost = (TCHAR*)env->GetStringChars(urlHost, 0);

	std::string result = AdblockMatchCssByAll(lpszUrl, lpszUrlHost);

	env->ReleaseStringChars(url, (jchar *)lpszUrl);
	env->ReleaseStringChars(urlHost, (jchar *)lpszUrlHost);

	return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_polar_browser_jni_NativeManager_initNativeQueryData(JNIEnv* env, jobject thiz, jint type, jstring data) {

	void* pData = 0;

	pData = (void*)env->GetStringUTFChars(data, 0);

	load(type, pData);

	env->ReleaseStringUTFChars(data, (char *)pData);
}

JNIEXPORT jstring JNICALL
Java_com_polar_browser_jni_NativeManager_addressInput(JNIEnv* env, jobject thiz, jbyteArray data) {

	char* pszData = (char*)env->GetByteArrayElements(data, 0);

	TCHAR* lpszData = UTF8Char2UnicodeChar(pszData);
	std::string result = input(lpszData);

	// 释放内存
	env->ReleaseByteArrayElements(data, (signed char*)pszData, 0);

	return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_polar_browser_jni_NativeManager_addItem(JNIEnv* env, jobject thiz, jint type,
			jbyteArray title, jstring url, jint navType, jlong time) {

	char* pszTitle = (char*)env->GetByteArrayElements(title, 0);
	TCHAR* lpszTitle = UTF8Char2UnicodeChar(pszTitle);

	LPCTSTR lpszUrl = (TCHAR*)env->GetStringChars(url, 0);

	addItem(type, lpszTitle, lpszUrl, navType, time);

	// 释放内存，防止泄露
	delete lpszTitle;
	env->ReleaseByteArrayElements(title, (signed char*)pszTitle, 0);
	env->ReleaseStringChars(url, (jchar*) lpszUrl);
}


JNIEXPORT void Java_com_polar_browser_jni_NativeManager_init(JNIEnv *env, jclass jcl,
															jobject context_object) {
	Util mUtil(env);

	jclass context_class = env->GetObjectClass(context_object);

	//context.getPackageManager()
	jmethodID methodId = env->GetMethodID(context_class, "getPackageManager",
										  "()Landroid/content/pm/PackageManager;");
	jobject package_manager_object = env->CallObjectMethod(context_object, methodId);
	if (package_manager_object == NULL) {
		env->DeleteLocalRef(context_class);
		return;
	}



	//context.getPackageName()
	methodId = env->GetMethodID(context_class, "getPackageName", "()Ljava/lang/String;");
	env->DeleteLocalRef(context_class);
	jstring package_name_string = (jstring) env->CallObjectMethod(context_object, methodId);
	if (package_name_string == NULL) {
		env->DeleteLocalRef(package_manager_object);
		return;
	}

	const char *package_name_str = env->GetStringUTFChars(package_name_string, 0);
	if (strcmp("com.polar.browser", package_name_str) != 0) {
		env->DeleteLocalRef(package_manager_object);
		env->ReleaseStringUTFChars(package_name_string,package_name_str);
		mUtil.forceExit();
		return;
	}


	//PackageManager.getPackageInfo(Sting, int)
	jclass pack_manager_class = env->GetObjectClass(package_manager_object);
	methodId = env->GetMethodID(pack_manager_class, "getPackageInfo",
								"(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
	env->DeleteLocalRef(pack_manager_class);
	jobject package_info_object = env->CallObjectMethod(package_manager_object, methodId,
														package_name_string, 64);

	if (package_info_object == NULL) {
		env->DeleteLocalRef(package_manager_object);
		env->ReleaseStringUTFChars(package_name_string,package_name_str);
		return;
	}
	env->ReleaseStringUTFChars(package_name_string,package_name_str);
	env->DeleteLocalRef(package_manager_object);

	//PackageInfo.signatures[0]
	jclass package_info_class = env->GetObjectClass(package_info_object);
	jfieldID fieldId = env->GetFieldID(package_info_class, "signatures",
									   "[Landroid/content/pm/Signature;");
	env->DeleteLocalRef(package_info_class);
	jobjectArray signature_object_array = (jobjectArray) env->GetObjectField(package_info_object,
																			 fieldId);
	if (signature_object_array == NULL) {
		env->DeleteLocalRef(package_info_object);
		return;
	}
	jobject signature_object = env->GetObjectArrayElement(signature_object_array, 0);

	env->DeleteLocalRef(package_info_object);
	env->DeleteLocalRef(signature_object_array);

	//Signature.toCharsString()
	jclass signature_class = env->GetObjectClass(signature_object);
	methodId = env->GetMethodID(signature_class, "toCharsString", "()Ljava/lang/String;");
	env->DeleteLocalRef(signature_class);
	jstring signature_string = (jstring) env->CallObjectMethod(signature_object, methodId);


	const char *str = env->GetStringUTFChars(signature_string, 0);
//    LOGI("sign:=%s,signlength=%d",str,strlen(str));

	const char *cut_str = str + (strlen(str) - 32);

//    LOGI("result=%s", cut_str);
	env->ReleaseStringUTFChars(signature_string,str);
	jclass appEnvClass = env->FindClass("com/polar/browser/env/AppEnv");
	if(appEnvClass==NULL) {
//        LOGI("%s", "buildConfigClass=NULL");
		env->DeleteLocalRef(signature_object);
		return;
	}
	env->DeleteLocalRef(signature_object);
	jfieldID debug = env->GetStaticFieldID(appEnvClass, "DEBUG", "Z");
	if(debug==NULL) {
		env->DeleteLocalRef(appEnvClass);
//        LOGI("%s", "debug=NULL");
		return;
	}
	jboolean isDebug = env->GetStaticBooleanField(appEnvClass, debug);
	env->DeleteLocalRef(appEnvClass);


	if(isDebug) {
//        LOGI("%s", "debug 版本");
		return;
	}


	if (strcmp("825ec96759e8ddecfb694957a9adacbb", cut_str) == 0) {

//        LOGI("%s", "通过验证");
		// 初始化ThreadManager
		jclass threadManager = env->FindClass("com/polar/browser/manager/ThreadManager");
		if (NULL == threadManager) {
			return;
		}

		jmethodID init = env->GetStaticMethodID(threadManager, "init", "()V");
		if (NULL == init) {
			env->DeleteLocalRef(threadManager); // 删除类指引
			return;
		}

		env->CallStaticVoidMethod(threadManager, init);

		// 初始化ConfigWrapper
		jclass configWrapper = env->FindClass("com/polar/browser/utils/ConfigWrapper");
		if (NULL == configWrapper) {
			return;
		}

		jmethodID init2 = env->GetStaticMethodID(configWrapper, "init", "()V");
		if (NULL == init2) {
			env->DeleteLocalRef(configWrapper); // 删除类指引
			return;
		}

		env->CallStaticVoidMethod(configWrapper, init2);
	} else {
		mUtil.forceExit();
	}

}