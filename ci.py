# !/usr/bin/python
# -*- coding: UTF-8 -*-

import os
import shutil
import time
from os import path, system


def printVar(tag, str):
    print "[%s:] %s" % (tag, str)


def printLineSeparator(tag, separator="--------------------"):
    print ""
    print "%s %s %s" % (separator, tag, separator)
    print ""


def printWithFormat(format, *args):
    print  format % args


def rename(dir, extention, addation):
    """rename files with extention in dir, add addation before extention as new name """
    printVar("rename: dir", dir)
    printVar("rename: extention", extention)
    printVar("rename: addation", addation)

    for parent, dirnames, filenames in os.walk(dir):
        for filename in filenames:
            if (filename.endswith(extention)):
                length = len(extention)
                newName = filename[0:-length] + addation + filename[-length:]
                originalFilePath = path.join(dir, filename)
                newFilePath = path.join(dir, newName)
                printVar("rename: originalFilePath", originalFilePath)
                printVar("rename: newFilePath", newFilePath)
                os.rename(originalFilePath, newFilePath)


def copyfile(sourceDir, destinationDir, filename):
    """copy file from sourceDir to destinationDir"""
    src = os.path.join(sourceDir, filename)
    dest = os.path.join(destinationDir, filename)
    if path.exists(dest):
        printWithFormat("%s exists in %s", filename, destinationDir)
    else:
        print ("copy %s from %s to %s") % (filename, sourceDir, destinationDir)
        shutil.copyfile(src, dest)


# environment virables
printLineSeparator("environment virables start")
jobName = os.environ["JOB_NAME"]
buildNumber = os.environ["BUILD_NUMBER"]
gitBranch = os.environ["GIT_BRANCH"]
gitCommit = os.environ["GIT_COMMIT"]
workSpace = os.environ["WORKSPACE"]
printVar("jobName", jobName)
printVar("buildNumber", buildNumber)
printVar("gitBranch", gitBranch)
printVar("gitCommit", gitCommit)
printVar("workSpace", workSpace)

printLineSeparator("environment virables end")
# ci build information
printLineSeparator("buildInfo start")
buildInfo = ("[%4s]%s[%s][%s]") % (
    buildNumber.zfill(4), time.strftime("[%Y-%m-%d][%H.%M.%S]", time.localtime()),
    gitBranch.split("/")[1],
    gitCommit[:8])

printVar("buildInfo", buildInfo)
printLineSeparator("buildInfo end")

# source code config
printLineSeparator("sourc code config start")
sourceDir = workSpace
outputs = path.join("polarbrowser", "build", "outputs")
outputsDir = path.join(sourceDir, outputs)
vcbrowserDir = path.join(sourceDir, "polarbrowser")

printVar("sourceDir", sourceDir)
printVar("outputs", outputs)
printVar("outputsDir", outputsDir)
printVar("vcbrowserDir", vcbrowserDir)
printLineSeparator("sourc code config end")
# ci publish config
printLineSeparator("ci publish config start")
ciPublishDir = path.join("G:\\", "backup", "CIPublish")
ciJobDir = path.join(ciPublishDir, jobName)
ciBuildDir = path.join(ciJobDir, buildInfo)
ciApkDir = path.join(ciBuildDir, "apk")

printVar("ciPublishDir", ciPublishDir)
printVar("ciJobDir", ciJobDir)
printVar("ciBuildDir", ciBuildDir)
printVar("ciApkDir", ciApkDir)
printLineSeparator("ci publish config end")

# signature config
printLineSeparator("signature config start")
signatureFile = "momeng.keystore"
signatureConfig = "signing.properties"

if path.exists(path.join(vcbrowserDir, signatureFile)):
    printVar("signature", "signature file exist in vcbrowser.")
else:
    if "signatureDir" in os.environ:
        signatureDir = os.environ["signatureDir"]
    else:
        signatureDir = path.dirname(path.dirname(sourceDir))
        if not path.exists(signatureDir):
            printVar("signatureDir", "signatureDir not exist")
            system.exit(1)
    printVar("signatureDir", signatureDir)
    copyfile(signatureDir, vcbrowserDir, signatureFile)
if path.exists(path.join(vcbrowserDir, signatureConfig)):
    printVar("signature", "signatureConfig file exist.")
else:
    copyfile(signatureDir, vcbrowserDir, signatureConfig)

printLineSeparator("signature config end")

# build apk
printLineSeparator("build apk start")
os.system('gradle clean assembleDebug assembleRelease')
printLineSeparator("build apk end")
printLineSeparator("after build start")
# copy file from source to destination
shutil.copytree(outputsDir, ciBuildDir)
# rename .apk file
rename(ciApkDir, ".apk", buildInfo)
printLineSeparator("after build end")
