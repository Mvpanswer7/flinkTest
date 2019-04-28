package flinkflow.conf

import java.util.Properties

object Connectors {
  val kafkaHadoopProperties = new Properties()
    kafkaHadoopProperties.put("bootstrap.servers","192.168.13.137:9092")
  val kafkaConfluentProperties = new Properties()
  kafkaConfluentProperties.put("bootstrap.servers","192.168.200.176:9092,192.168.200.178:9092,192.168.200.179:9092")
  kafkaConfluentProperties.put("request.timeout.ms","60000")
  kafkaConfluentProperties.put("linger.ms","1000")
  kafkaConfluentProperties.put("batch.size","30000")
}
