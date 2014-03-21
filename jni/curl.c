/* vim: set sw=4 ts=4:
 * Author: Liu DongMiao <liudongmiao@gmail.com>
 * Created  : Thu 26 Jul 2012 02:13:55 PM CST
 * Modified : Sun 05 Aug 2012 06:49:01 AM CST
 *
 * CopyRight (c) 2012, Liu DongMiao, <liudongmiao@gmail.com>.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include "curl/curl.h"

#include <jni.h>
#include <android/log.h>
#define TAG "CURL-C"
#define LOGE(...) (__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__))
#define LOGD(...) (__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__))

#define CLASSNAME "me/piebridge/curl/Curl"
#define ARRAY_SIZE(x) (sizeof(x) / sizeof(x[0]))

static CURLcode code;

static jint curl_init(JNIEnv *env, jobject clazz)
{
	CURL *curl = curl_easy_init();
	return (int)curl;
}

static jboolean curl_setopt_long(JNIEnv *env, jobject clazz, jint handle, jint option, jlong value)
{
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return JNI_FALSE;
	}

	switch (option) {
		case CURLOPT_SSLENGINE_DEFAULT:
		case CURLOPT_DNS_CACHE_TIMEOUT:
		case CURLOPT_DNS_USE_GLOBAL_CACHE:
		case CURLOPT_MAXCONNECTS:
		case CURLOPT_FORBID_REUSE:
		case CURLOPT_FRESH_CONNECT:
		case CURLOPT_VERBOSE:
		case CURLOPT_HEADER:
		case CURLOPT_NOPROGRESS:
		case CURLOPT_NOBODY:
		case CURLOPT_FAILONERROR:
		case CURLOPT_UPLOAD:
		case CURLOPT_PUT:
		case CURLOPT_FILETIME:
		case CURLOPT_FTP_CREATE_MISSING_DIRS:
		case CURLOPT_SERVER_RESPONSE_TIMEOUT:
		case CURLOPT_TFTP_BLKSIZE:
		case CURLOPT_DIRLISTONLY:
		case CURLOPT_APPEND:
		case CURLOPT_FTP_FILEMETHOD:
		case CURLOPT_NETRC:
		case CURLOPT_TRANSFERTEXT:
		case CURLOPT_TIMECONDITION:
		case CURLOPT_TIMEVALUE:
		case CURLOPT_SSLVERSION:
		case CURLOPT_AUTOREFERER:
		case CURLOPT_TRANSFER_ENCODING:
		case CURLOPT_FOLLOWLOCATION:
		case CURLOPT_UNRESTRICTED_AUTH:
		case CURLOPT_MAXREDIRS:
		case CURLOPT_POSTREDIR:
		case CURLOPT_POST:
		case CURLOPT_POSTFIELDSIZE:
		case CURLOPT_COOKIESESSION:
		case CURLOPT_HTTPGET:
		case CURLOPT_HTTP_VERSION:
		case CURLOPT_HTTPPROXYTUNNEL:
		case CURLOPT_PROXYPORT:
		case CURLOPT_PROXYTYPE:
		case CURLOPT_PROXY_TRANSFER_MODE:
		case CURLOPT_SOCKS5_GSSAPI_NEC:
		case CURLOPT_FTP_USE_EPRT:
		case CURLOPT_FTP_USE_EPSV:
		case CURLOPT_FTP_USE_PRET:
		case CURLOPT_FTP_SSL_CCC:
		case CURLOPT_FTP_SKIP_PASV_IP:
		case CURLOPT_INFILESIZE:
		case CURLOPT_LOW_SPEED_LIMIT:
		case CURLOPT_LOW_SPEED_TIME:
		case CURLOPT_PORT:
		case CURLOPT_TIMEOUT:
		case CURLOPT_TIMEOUT_MS:
		case CURLOPT_CONNECTTIMEOUT:
		case CURLOPT_CONNECTTIMEOUT_MS:
		case CURLOPT_ACCEPTTIMEOUT_MS:
		case CURLOPT_RESUME_FROM:
		case CURLOPT_CRLF:
		case CURLOPT_LOCALPORT:
		case CURLOPT_LOCALPORTRANGE:
		case CURLOPT_GSSAPI_DELEGATION:
		case CURLOPT_SSL_VERIFYPEER:
		case CURLOPT_SSL_VERIFYHOST:
		case CURLOPT_CERTINFO:
		case CURLOPT_BUFFERSIZE:
		case CURLOPT_NOSIGNAL:
		case CURLOPT_MAXFILESIZE:
		case CURLOPT_USE_SSL:
		case CURLOPT_SSL_OPTIONS:
		case CURLOPT_FTPSSLAUTH:
		case CURLOPT_IPRESOLVE:
		case CURLOPT_TCP_NODELAY:
		case CURLOPT_IGNORE_CONTENT_LENGTH:
		case CURLOPT_CONNECT_ONLY:
		case CURLOPT_SSL_SESSIONID_CACHE:
		case CURLOPT_SSH_AUTH_TYPES:
		case CURLOPT_HTTP_TRANSFER_DECODING:
		case CURLOPT_HTTP_CONTENT_DECODING:
		case CURLOPT_NEW_FILE_PERMS:
		case CURLOPT_NEW_DIRECTORY_PERMS:
		case CURLOPT_ADDRESS_SCOPE:
		case CURLOPT_PROTOCOLS:
		case CURLOPT_REDIR_PROTOCOLS:
		case CURLOPT_RTSP_REQUEST:
		case CURLOPT_RTSP_CLIENT_CSEQ:
		case CURLOPT_RTSP_SERVER_CSEQ:
		case CURLOPT_WILDCARDMATCH:
		case CURLOPT_TCP_KEEPALIVE:
		case CURLOPT_TCP_KEEPIDLE:
		case CURLOPT_TCP_KEEPINTVL:
		/* curl_off_t is int64_t, jlong */
		case CURLOPT_POSTFIELDSIZE_LARGE:
		case CURLOPT_INFILESIZE_LARGE:
		case CURLOPT_MAX_SEND_SPEED_LARGE:
		case CURLOPT_MAX_RECV_SPEED_LARGE:
		case CURLOPT_RESUME_FROM_LARGE:
		case CURLOPT_MAXFILESIZE_LARGE:
			code = curl_easy_setopt(curl, option, value);
			if (CURLE_OK == code) {
				return JNI_TRUE;
			}
			break;
		/* unsigned long */
		case CURLOPT_HTTPAUTH:
		case CURLOPT_PROXYAUTH:
			code = curl_easy_setopt(curl, option, (unsigned long)value);
			if (CURLE_OK == code) {
				return JNI_TRUE;
			}
	}
	return JNI_FALSE;
}

static jboolean curl_setopt_string(JNIEnv *env, jobject clazz, jint handle, jint option, jstring value)
{
	FILE *file;
	char *param;
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return JNI_FALSE;
	}

	switch (option) {
		case CURLOPT_SSL_CIPHER_LIST:
		case CURLOPT_RANDOM_FILE:
		case CURLOPT_EGDSOCKET:
		case CURLOPT_NETRC_FILE:
		case CURLOPT_ACCEPT_ENCODING:
		case CURLOPT_COPYPOSTFIELDS:
		case CURLOPT_REFERER:
		case CURLOPT_USERAGENT:
		case CURLOPT_COOKIE:
		case CURLOPT_COOKIEJAR:
		case CURLOPT_COOKIELIST:
		case CURLOPT_CUSTOMREQUEST:
		case CURLOPT_PROXY:
		case CURLOPT_SOCKS5_GSSAPI_SERVICE:
		case CURLOPT_ERRORBUFFER:
		case CURLOPT_FTPPORT:
		case CURLOPT_URL:
		case CURLOPT_USERPWD:
		case CURLOPT_USERNAME:
		case CURLOPT_PASSWORD:
		case CURLOPT_PROXYUSERPWD:
		case CURLOPT_PROXYUSERNAME:
		case CURLOPT_PROXYPASSWORD:
		case CURLOPT_NOPROXY:
		case CURLOPT_RANGE:
		case CURLOPT_SSLCERT:
		case CURLOPT_SSLCERTTYPE:
		case CURLOPT_SSLKEY:
		case CURLOPT_SSLKEYTYPE:
		case CURLOPT_KEYPASSWD:
		case CURLOPT_SSLENGINE:
		case CURLOPT_INTERFACE:
		case CURLOPT_KRBLEVEL:
		case CURLOPT_CAINFO:
		case CURLOPT_CAPATH:
		case CURLOPT_CRLFILE:
		case CURLOPT_ISSUERCERT:
		case CURLOPT_FTP_ACCOUNT:
		case CURLOPT_FTP_ALTERNATIVE_TO_USER:
		case CURLOPT_SSH_PUBLIC_KEYFILE:
		case CURLOPT_SSH_PRIVATE_KEYFILE:
		case CURLOPT_SSH_HOST_PUBLIC_KEY_MD5:
		case CURLOPT_SSH_KNOWNHOSTS:
		case CURLOPT_MAIL_FROM:
		case CURLOPT_MAIL_AUTH:
		case CURLOPT_RTSP_SESSION_ID:
		case CURLOPT_RTSP_STREAM_URI:
		case CURLOPT_RTSP_TRANSPORT:
		case CURLOPT_TLSAUTH_USERNAME:
		case CURLOPT_TLSAUTH_PASSWORD:
		case CURLOPT_TLSAUTH_TYPE:
		case CURLOPT_DNS_SERVERS:
			param = (char *)(*env)->GetStringUTFChars(env, value, NULL);
			code = curl_easy_setopt(curl, option, param);
			(*env)->ReleaseStringUTFChars(env, value, param);
			if (CURLE_OK == code) {
				return JNI_TRUE;
			}
			break;
		/* FILE */
		case CURLOPT_FILE:
		case CURLOPT_INFILE:
		case CURLOPT_STDERR:
		case CURLOPT_WRITEHEADER:
			param = (char *)(*env)->GetStringUTFChars(env, value, NULL);
			file = fopen(param, "wb");
			(*env)->ReleaseStringUTFChars(env, value, param);
			if(NULL == file) {
				LOGE("cannot open: %s", strerror(errno));
				return JNI_FALSE;
			}
			code = curl_easy_setopt(curl, option, file);
			if (CURLE_OK == code) {
				return JNI_TRUE;
			}
			break;
	}
	return JNI_FALSE;
}

static struct curl_slist *slists = NULL;
static void append(struct curl_slist *slist)
{
	struct curl_slist *head;
	if (NULL == slists) {
		slists = slist;
	} else {
		head = slists;
		while (head->next) {
			head = head->next;
		}
		head->next = slist;
	}
}

static jboolean curl_setopt_array(JNIEnv *env, jobject clazz, jint handle, jint option, jobjectArray value)
{
	int i, count;
	struct curl_slist *slist = NULL;
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return JNI_FALSE;
	}

	switch (option) {
		case CURLOPT_HTTPHEADER:
		case CURLOPT_HTTP200ALIASES:
		case CURLOPT_POSTQUOTE:
		case CURLOPT_PREQUOTE:
		case CURLOPT_QUOTE:
		case CURLOPT_RESOLVE:
		case CURLOPT_TELNETOPTIONS:
		case CURLOPT_MAIL_RCPT:
			break;
		default:
			return JNI_FALSE;
	}

	count = (*env)->GetArrayLength(env, value);

	for (i = 0; i < count; i++) {
		jstring string = (jstring)(*env)->GetObjectArrayElement(env, value, i);
		char *param = (char *)(*env)->GetStringUTFChars(env, string, NULL);
		slist = curl_slist_append(slist, param);
		(*env)->ReleaseStringUTFChars(env, value, param);
		if (NULL == slist) {
			return JNI_FALSE;
		}
	}

	code = curl_easy_setopt(curl, option, slist);
	if (CURLE_OK != code) {
		curl_slist_free_all(slist);
		return JNI_FALSE;
	}

	append(slist);

	return JNI_TRUE;
}

static jboolean curl_setopt_bytes(JNIEnv *env, jobject clazz, jint handle, jint option, jbyteArray value)
{
	jbyte *data;
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return JNI_FALSE;
	}

	switch (option) {
		case CURLOPT_POSTFIELDS:
		case CURLOPT_COOKIEFILE:
		case CURLOPT_WRITEHEADER:
		case CURLOPT_PROGRESSDATA:
		case CURLOPT_DEBUGDATA:
		case CURLOPT_SEEKDATA:
		case CURLOPT_IOCTLDATA:
		case CURLOPT_SSL_CTX_DATA:
		case CURLOPT_PRIVATE:
		case CURLOPT_SOCKOPTDATA:
		case CURLOPT_OPENSOCKETDATA:
		case CURLOPT_CLOSESOCKETDATA:
		case CURLOPT_SSH_KEYDATA:
		case CURLOPT_INTERLEAVEDATA:
		case CURLOPT_CHUNK_DATA:
		case CURLOPT_FNMATCH_DATA:
		/* void */
		case CURLOPT_FILE:
		case CURLOPT_INFILE:
		case CURLOPT_STDERR:
			break;
		default:
			return JNI_FALSE;
	}

	data = (*env)->GetByteArrayElements(env, value, NULL);
	if (data == NULL) {
		LOGE("cannot get byte array elements");
		return JNI_FALSE;
	}

	code = curl_easy_setopt(curl, option, data);

	if (CURLE_OK != code) {
		(*env)->ReleaseByteArrayElements(env, value, data, 0);
		return JNI_FALSE;
	}

	LOGD("i dont find anyway to release the elements");
	// or memcpy, but cannot free neither...
	return JNI_TRUE;
}

typedef struct {
	jobject object;
	jmethodID method;
} jfunction;

typedef struct {
	JavaVM *vm;
	jint version;
	jfunction read;
	jfunction write;
	jfunction header;
	jfunction debug;
	jfunction progress;
} callback_t;

#define SIG_READ "([B)I"
#define SIG_WRITE "([B)I"
#define SIG_DEBUG "(I[B)I"
#define SIG_PROGRESS "(DDDD)I"

static callback_t *callback = NULL;
#define jvm callback->vm
#define readobject callback->read.object
#define readmethod callback->read.method
#define writeobject callback->write.object
#define writemethod callback->write.method
#define headerobject callback->header.object
#define headermethod callback->header.method
#define debugobject callback->debug.object
#define debugmethod callback->debug.method
#define progressobject callback->progress.object
#define progressmethod callback->progress.method


#ifdef DEBUG
#define JLOGD LOGD
#else
#define JLOGD(...) ((void)0)
#endif

#define assert(x) do { \
	if (0 == x) {\
		LOGE("%s: " #x " is null", __FUNCTION__); return 0; \
	}\
} while (0)

#define setcallback(type) \
	JNIEnv *env; \
	jobject object = type##object; \
	jmethodID method = type##method; \
	JLOGD("%s %s +%d", __FUNCTION__, __FILE__, __LINE__); \
	assert(object); \
	JLOGD("%s %s +%d, object=%p", __FUNCTION__, __FILE__, __LINE__, object); \
	assert(method); \
	JLOGD("%s %s +%d, method=%p", __FUNCTION__, __FILE__, __LINE__, method); \
	(*jvm)->GetEnv(jvm, (void**)&env, callback->version); \
	JLOGD("%s %s +%d, env=%p", __FUNCTION__, __FILE__, __LINE__, env)

static size_t curl_read(char *ptr, size_t size, size_t nmemb, void *userdata)
{
	jbyte *data;
	jint result;
	jbyteArray array;
	jint length = size * nmemb;

	setcallback(read);

	if (length == 0) {
		LOGE("curl_header length is 0");
		return 0;
	}

	array = (*env)->NewByteArray(env, length);
	if (!array) {
		LOGE("curl_read could not create new byte[]");
		return 0;
	}
	result = (*env)->CallIntMethod(env, object, method, array);
	data = (*env)->GetByteArrayElements(env, array, NULL);
	memcpy(ptr, data, result);
	(*env)->ReleaseByteArrayElements(env, array, data, 0);
	(*env)->DeleteLocalRef(env, array);

	return result;
}

static size_t curl_write(char *ptr, size_t size, size_t nmemb, void *userdata)
{
	jint result;
	jbyteArray array;
	jint length = size * nmemb;

	setcallback(write);

	if (length == 0) {
		LOGE("curl_header length is 0");
		return 0;
	}

	array = (*env)->NewByteArray(env, length);
	if (!array) {
		LOGE("curl_write could not create new byte[]");
		return 0;
	}
	(*env)->SetByteArrayRegion(env, array, 0, length, (jbyte *)ptr);
	result = (*env)->CallIntMethod(env, object, method, array);
	(*env)->DeleteLocalRef(env, array);

	return result;
}

static size_t curl_header(char *ptr, size_t size, size_t nmemb, void *userdata)
{
	jint result;
	jbyteArray array;
	jint length = size * nmemb;

	setcallback(header);

	if (length == 0) {
		LOGE("curl_header length is 0");
		return 0;
	}

	array = (*env)->NewByteArray(env, length);
	if (!array) {
		LOGE("curl_header could not create new byte[]");
		return 0;
	}
	(*env)->SetByteArrayRegion(env, array, 0, length, (jbyte *)ptr);
	result = (*env)->CallIntMethod(env, object, method, array);
	(*env)->DeleteLocalRef(env, array);

	return result;
}

static int curl_debug(CURL *handle, curl_infotype type, char *data, size_t size, void *userptr)
{
	jint result;
	jbyteArray array;

	setcallback(debug);

	if (size == 0) {
		LOGD("curl_debug size is 0");
		return 0;
	}

	array = (*env)->NewByteArray(env, size);
	if (!array) {
		LOGE("curl_debug could not create new byte[]");
		return 0;
	}
	(*env)->SetByteArrayRegion(env, array, 0, size, (jbyte *)data);
	result = (*env)->CallIntMethod(env, object, method, type, array);
	(*env)->DeleteLocalRef(env, array);

	return result;
}

static int curl_progress(void *clientp, double dltotal, double dlnow, double ultotal, double ulnow)
{
	jint result;

	setcallback(progress);

	result = (*env)->CallIntMethod(env, object, method, dltotal, dlnow, ultotal, ulnow);

	return result;
}

static jboolean curl_setopt_callback(JNIEnv *env, jobject clazz, jint handle, jint option, jobject value)
{
	const char *sig;
	jclass class;
	jmethodID method;
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return JNI_FALSE;
	}

	switch (option) {
		case CURLOPT_READFUNCTION:
			sig = SIG_READ;
			break;
		case CURLOPT_WRITEFUNCTION:
		case CURLOPT_HEADERFUNCTION:
			sig = SIG_WRITE;
			break;
		case CURLOPT_DEBUGFUNCTION:
			sig = SIG_DEBUG;
			break;
		case CURLOPT_PROGRESSFUNCTION:
			sig = SIG_PROGRESS;
			break;
		default:
			return JNI_FALSE;
	}

	class = (*env)->GetObjectClass(env, value);
	method = (*env)->GetMethodID(env, class, "callback", sig);
	if (method == 0) {
		LOGE("%s: cannot find function callback %s", __FUNCTION__, sig);
		return JNI_FALSE;
	}

	switch (option) {
		case CURLOPT_READFUNCTION:
			LOGD("set readfunction");
			readobject = (*env)->NewGlobalRef(env, value);
			readmethod = method;
			curl_easy_setopt(curl, option, curl_read);
			break;
		case CURLOPT_WRITEFUNCTION:
			LOGD("set writefunction");
			writeobject = (*env)->NewGlobalRef(env, value);
			writemethod = method;
			curl_easy_setopt(curl, option, curl_write);
			break;
		case CURLOPT_HEADERFUNCTION:
			LOGD("set headerfunction");
			headerobject = (*env)->NewGlobalRef(env, value);
			headermethod = method;
			curl_easy_setopt(curl, option, curl_header);
			break;
		case CURLOPT_DEBUGFUNCTION:
			LOGD("set debugfunction");
			debugobject = (*env)->NewGlobalRef(env, value);
			debugmethod = method;
			curl_easy_setopt(curl, option, curl_debug);
			break;
		case CURLOPT_PROGRESSFUNCTION:
			LOGD("set progressfunction");
			progressobject = (*env)->NewGlobalRef(env, value);
			progressmethod = method;
			curl_easy_setopt(curl, option, curl_progress);
			break;
	}

	return JNI_TRUE;
}

static jboolean curl_perform(JNIEnv *env, jobject clazz, jint handle)
{
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return JNI_FALSE;
	}

	code = curl_easy_perform(curl);

	if (NULL != slists) {
		curl_slist_free_all(slists);
		slists = NULL;
	}

	fflush(NULL);
	if (CURLE_OK == code) {
		return JNI_TRUE;
	}

	return JNI_FALSE;
}

static jlong curl_getinfo_long(JNIEnv *env, jobject clazz, jint handle, jint info)
{
	long longp;
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return -1;
	}

	switch (info) {
		case CURLINFO_RESPONSE_CODE:
		case CURLINFO_HEADER_SIZE:
		case CURLINFO_REQUEST_SIZE:
		case CURLINFO_SSL_VERIFYRESULT:
		case CURLINFO_FILETIME:
		case CURLINFO_REDIRECT_COUNT:
		case CURLINFO_HTTP_CONNECTCODE:
		case CURLINFO_HTTPAUTH_AVAIL:
		case CURLINFO_PROXYAUTH_AVAIL:
		case CURLINFO_OS_ERRNO:
		case CURLINFO_NUM_CONNECTS:
		case CURLINFO_LASTSOCKET:
		case CURLINFO_CONDITION_UNMET:
		case CURLINFO_RTSP_CLIENT_CSEQ:
		case CURLINFO_RTSP_SERVER_CSEQ:
		case CURLINFO_RTSP_CSEQ_RECV:
		case CURLINFO_PRIMARY_PORT:
		case CURLINFO_LOCAL_PORT:
			code = curl_easy_getinfo(curl, info, &longp);
			if (CURLE_OK == code) {
				return longp;
			}
			break;
	}

	return -1;
}

static jdouble curl_getinfo_double(JNIEnv *env, jobject clazz, jint handle, jint info)
{
	double doublep;
	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return -1;
	}

	switch (info) {
		case CURLINFO_TOTAL_TIME:
		case CURLINFO_NAMELOOKUP_TIME:
		case CURLINFO_CONNECT_TIME:
		case CURLINFO_PRETRANSFER_TIME:
		case CURLINFO_SIZE_UPLOAD:
		case CURLINFO_SIZE_DOWNLOAD:
		case CURLINFO_SPEED_DOWNLOAD:
		case CURLINFO_SPEED_UPLOAD:
		case CURLINFO_CONTENT_LENGTH_DOWNLOAD:
		case CURLINFO_CONTENT_LENGTH_UPLOAD:
		case CURLINFO_STARTTRANSFER_TIME:
		case CURLINFO_REDIRECT_TIME:
		case CURLINFO_APPCONNECT_TIME:
			code = curl_easy_getinfo(curl, info, &doublep);
			if (CURLE_OK == code) {
				return doublep;
			}
			break;
	}

	return -1;
}

static jobjectArray dump_slist(JNIEnv *env, struct curl_slist *slist)
{
	jclass class;
	jsize index, len = 0;
	jobjectArray result;
	struct curl_slist *head;

	if (slist == NULL) {
		return NULL;
	}

	head = slist;
	while (head) {
		len++;
		head = head->next;
	}

	class = (*env)->FindClass(env, "java/lang/String");
	if (NULL == class) {
		LOGE("no such class: java/lang/String");
		return NULL;
	}

	result = (*env)->NewObjectArray(env, len, class, NULL);
	if (result == NULL) {
		LOGE("cannot create String Array");
		return NULL;
	}

	for (index = 0, head = slist; index < len; index++, head = head->next) {
		jstring string = (*env)->NewStringUTF(env, head->data);
		if (string == NULL) {
			LOGE("cannot create String for %d in slist: %s", index, head->data);
			(*env)->DeleteLocalRef(env, result);
			return NULL;
		}
		(*env)->SetObjectArrayElement(env, result, index, string);
		(*env)->DeleteLocalRef(env, string);
	}

	return result;
}

static jobjectArray dump_certinfo(JNIEnv *env, struct curl_certinfo *certinfo)
{
	jsize index;
	jclass class;
	jobjectArray result;

	if (certinfo->num_of_certs == 0) {
		return NULL;
	}

	// why there is no [java/lang/String?
	class = (*env)->FindClass(env, "java/lang/Object");
	if (NULL == class) {
		LOGE("no such class: java/lang/Object");
		return NULL;
	}

	result = (*env)->NewObjectArray(env, certinfo->num_of_certs, class, NULL);
	if (result == NULL) {
		LOGE("cannot create Object[]");
		return NULL;
	}

	for (index = 0; index < certinfo->num_of_certs; index++) {
		jobjectArray object = dump_slist(env, certinfo->certinfo[index]);
		if (object == NULL) {
			LOGE("cannot create String[] for %d in certinfo %p", index, certinfo->certinfo[index]);
			(*env)->DeleteLocalRef(env, result);
			return NULL;
		}
		(*env)->SetObjectArrayElement(env, result, index, object);
	}

	return result;
}

static jobjectArray curl_getinfo_slist(JNIEnv *env, jobject clazz, jint handle, jint info)
{
	jobjectArray result = NULL;
	union {
		struct curl_certinfo *to_certinfo;
		struct curl_slist *to_slist;
	} ptr;

	CURL *curl = (CURL *)handle;
	if (NULL == curl) {
		return NULL;
	}

	switch (info) {
		case CURLINFO_COOKIELIST:
		case CURLINFO_SSL_ENGINES:
		case CURLINFO_CERTINFO:
			code = curl_easy_getinfo(curl, info, &(ptr.to_slist));
			if (CURLE_OK == code) {
				break;
			}
		default:
			return NULL;
	}

	switch (info) {
		case CURLINFO_COOKIELIST:
		case CURLINFO_SSL_ENGINES:
			result = dump_slist(env, ptr.to_slist);
			curl_slist_free_all(ptr.to_slist);
			break;
		case CURLINFO_CERTINFO:
			result = dump_certinfo(env, ptr.to_certinfo);
			break;
	}

	return result;
}

static jstring curl_getinfo(JNIEnv *env, jobject clazz, jint handle, jint info)
{
	long longp;
	double doublep;
	char value[64];
	char *charp;
	CURL *curl = (CURL *)handle;

	if (NULL == curl) {
		return (*env)->NewStringUTF(env, "");
	}

	switch (info) {
		case CURLINFO_RESPONSE_CODE:
		case CURLINFO_HEADER_SIZE:
		case CURLINFO_REQUEST_SIZE:
		case CURLINFO_SSL_VERIFYRESULT:
		case CURLINFO_FILETIME:
		case CURLINFO_REDIRECT_COUNT:
		case CURLINFO_HTTP_CONNECTCODE:
		case CURLINFO_HTTPAUTH_AVAIL:
		case CURLINFO_PROXYAUTH_AVAIL:
		case CURLINFO_OS_ERRNO:
		case CURLINFO_NUM_CONNECTS:
		case CURLINFO_LASTSOCKET:
		case CURLINFO_CONDITION_UNMET:
		case CURLINFO_RTSP_CLIENT_CSEQ:
		case CURLINFO_RTSP_SERVER_CSEQ:
		case CURLINFO_RTSP_CSEQ_RECV:
		case CURLINFO_PRIMARY_PORT:
		case CURLINFO_LOCAL_PORT:
			code = curl_easy_getinfo(curl, info, &longp);
			if (CURLE_OK == code) {
				sprintf(value, "%ld", longp);
				return (*env)->NewStringUTF(env, value);
			}
			break;
		case CURLINFO_TOTAL_TIME:
		case CURLINFO_NAMELOOKUP_TIME:
		case CURLINFO_CONNECT_TIME:
		case CURLINFO_PRETRANSFER_TIME:
		case CURLINFO_SIZE_UPLOAD:
		case CURLINFO_SIZE_DOWNLOAD:
		case CURLINFO_SPEED_DOWNLOAD:
		case CURLINFO_SPEED_UPLOAD:
		case CURLINFO_CONTENT_LENGTH_DOWNLOAD:
		case CURLINFO_CONTENT_LENGTH_UPLOAD:
		case CURLINFO_STARTTRANSFER_TIME:
		case CURLINFO_REDIRECT_TIME:
		case CURLINFO_APPCONNECT_TIME:
			code = curl_easy_getinfo(curl, info, &doublep);
			if (CURLE_OK == code) {
				sprintf(value, "%.3f", doublep);
				return (*env)->NewStringUTF(env, value);
			}
			break;
		case CURLINFO_EFFECTIVE_URL:
		case CURLINFO_CONTENT_TYPE:
		case CURLINFO_PRIVATE:
		case CURLINFO_FTP_ENTRY_PATH:
		case CURLINFO_REDIRECT_URL:
		case CURLINFO_PRIMARY_IP:
		case CURLINFO_RTSP_SESSION_ID:
		case CURLINFO_LOCAL_IP:
			code = curl_easy_getinfo(curl, info, &charp);
			if (CURLE_OK == code) {
				return (*env)->NewStringUTF(env, charp);
			}
			break;
	}

	return (*env)->NewStringUTF(env, "");
}

static jint curl_errno(JNIEnv *env, jobject clazz)
{
	return code;
}

static jstring curl_error(JNIEnv *env, jobject clazz)
{
	return (*env)->NewStringUTF(env, curl_easy_strerror(code));
}

#define deleteref(x) do {\
	if (x) {\
		(*env)->DeleteGlobalRef(env, x);\
	}\
} while (0)
static void curl_cleanup(JNIEnv *env, jobject clazz, jint handle)
{
	CURL *curl = (CURL *)handle;
	if (NULL != curl) {
		curl_easy_cleanup(curl);
	}

	if (callback) {
		deleteref(readobject);
		deleteref(writeobject);
		deleteref(headerobject);
		deleteref(debugobject);
		deleteref(progressobject);
	}
}

static JNINativeMethod methods[] = {
	/* name, signature, funcPtr */
	{"curl_init", "()I", (void*)curl_init},
	{"curl_setopt", "(IIJ)Z", (void*)curl_setopt_long},
	{"curl_setopt", "(IILjava/lang/String;)Z", (void*)curl_setopt_string},
	{"curl_setopt", "(II[Ljava/lang/String;)Z", (void*)curl_setopt_array},
	{"curl_setopt", "(II[B)Z", (void*)curl_setopt_bytes},
	{"curl_setopt", "(IILjava/lang/Object;)Z", (void*)curl_setopt_callback},
	{"curl_perform", "(I)Z", (void*)curl_perform},
	{"curl_getinfo", "(II)Ljava/lang/String;", (void*)curl_getinfo},
	{"curl_getinfo_long", "(II)J", (void*)curl_getinfo_long},
	{"curl_getinfo_double", "(II)D", (void*)curl_getinfo_double},
	{"curl_getinfo_list", "(II)[Ljava/lang/String;", (void*)curl_getinfo_slist},
	{"curl_getinfo_certinfo", "(II)[Ljava/lang/Object;", (void*)curl_getinfo_slist},
	{"curl_cleanup", "(I)V", (void*)curl_cleanup},
	{"curl_errno", "()I", (void*)curl_errno},
	{"curl_error", "()Ljava/lang/String;", (void*)curl_error},
};

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
	JNIEnv *env;
	jclass clazz;

	if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}

	callback = (callback_t *)calloc(1, sizeof(callback_t));
	if (callback == NULL) {
		LOGE("%s out of memory", __FUNCTION__);
		return -1;
	}
	(*env)->GetJavaVM(env, &(callback->vm));
	callback->version = (*env)->GetVersion(env);

	clazz = (*env)->FindClass(env, CLASSNAME);
	if (clazz == NULL) {
		LOGE("Can't find %s", CLASSNAME);
		return -1;
	}

	if ((*env)->RegisterNatives(env, clazz, methods, ARRAY_SIZE(methods)) < 0) {
		return -1;
	}

	return JNI_VERSION_1_6;
}
