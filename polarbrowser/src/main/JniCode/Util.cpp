//
// Created by 梁赛飞 on 16/12/26.
//

#include "Util.h"

void Util::forceExit() {

    //android.os.Process.killProcess(android.os.Process.myPid());
    jclass processClass = env->FindClass("android/os/Process");
    if (NULL == processClass) {
        return;
    }
    jmethodID killProcessMethodId = env->GetStaticMethodID(processClass, "killProcess", "(I)V");
    if (NULL == killProcessMethodId) {
        env->DeleteLocalRef(processClass); // 删除类指引
        return;
    }
    env->CallStaticVoidMethod(processClass, killProcessMethodId);


    //		System.exit(1);
    jclass systemClass = env->FindClass("java/lang/System");
    if (NULL == systemClass) {
        return;
    }
    jmethodID exitMethodId = env->GetStaticMethodID(systemClass, "exit", "(I)V");
    if (NULL == exitMethodId) {
        env->DeleteLocalRef(systemClass);
        return;
    }
    env->CallStaticVoidMethod(systemClass, exitMethodId,1);

}

