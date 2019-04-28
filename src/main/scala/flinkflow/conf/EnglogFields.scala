package flinkflow.conf

object EnglogFields {

  val fieldsKafkaBasic = Array("k_id", "k_time")
  val fieldsEnglogLogs = Array("d_log", "urldet_log", "wifi_log")
  val fieldsEnglogHead = Array("log_id", "channel", "apptoken", "receive_time", "receiver_ip", "user_ip", "user_country",
    "user_province", "user_city", "user_latitude", "user_longitude", "user_isp", "user_port", "id", "system", "system_version",
    "phone_brand", "system_build", "device", "version", "extra_id", "android_id", "serial_number_p", "imei", "imei_p", "phone",
    "timezone", "system_language", "gps_lon", "gps_lat", "pkg", "log_length", "conf_id", "conf_version")
  val fieldsEnglogAppdet = Array("time", "app_type", "apk_md5", "apk_name", "vir_name", "vname", "vfamily", "vtype", "facilitated_vname",
    "time_consuming", "engine_version", "siglib_version", "scan_opt", "old_keyhash", "new_keyhash", "dex_md5", "mf_md5", "apk_size")
  val fieldsEnglogUrldet = Array("time", "url", "result", "time_consuming", "url_engine_version", "url_siglib_version", "tag", "content", "source")
  val fieldsEnglogWifi = Array("time", "ssid", "bssid", "security_type", "mitm", "arp", "evil_device")
  val fieldsEnglogJcTest = Array("time", "url", "result", "time_consuming", "url_engine_version", "url_siglib_version", "tag", "content", "source")

}
