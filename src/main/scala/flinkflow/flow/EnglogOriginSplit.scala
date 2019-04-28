package flinkflow.flow

import flinkflow.conf.EnglogFields._
import flinkflow.conf.Connectors._
import flinkflow.avro._
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.table.descriptors._
import org.apache.flink.api.common.typeinfo.Types
import org.apache.flink.types.Row
import org.apache.flink.table.api.scala._
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.TableEnvironment
import org.apache.flink.util.Collector

import scala.collection._
import scala.collection.JavaConversions._
import java.util.{List => JList, Map => JMap}

class EnglogOriginSplit(env: StreamExecutionEnvironment) extends BaseFlow {
  val tableEnv = TableEnvironment.getTableEnvironment(env)

  var englogOriginSchema = new Schema()
  var englogBasicSchema = new Schema()
  var englogAppdetSchema = new Schema()
  var englogUrldetSchema = new Schema()
  var englogWifiSchema = new Schema()
  var englogJcTestSchema = new Schema()

  val fieldsBasic = fieldsEnglogHead ++ Array("d_len")
  val fieldsAppdet = fieldsEnglogHead ++ fieldsEnglogAppdet
  val fieldsUrldet = fieldsEnglogHead ++ fieldsEnglogUrldet
  val fieldsWifi = fieldsEnglogHead ++ fieldsEnglogWifi
  val fieldsJcTest = fieldsEnglogHead ++ fieldsEnglogJcTest

  val tableSourceEnglogOrigin = "kafka_englog_origin"
  val tableSinkEnglogBasic = "kafka_englog_basic"
  val tableSinkEnglogAppdet = "kafka_englog_appdet"
  val tableSinkEnglogUrldet = "kafka_englog_urldet"
  val tableSinkEnglogWifi = "kafka_englog_wifi"
  val tableSinkJcTest = "kafka_jc_test"

  def init(): Unit = {
    fieldsEnglogHead.map(f => {
      englogOriginSchema.field(f, Types.STRING)
    })
    fieldsEnglogLogs.map(f => {
      englogOriginSchema.field(f, Types.LIST(Types.MAP(Types.STRING, Types.STRING)))
    })
    fieldsBasic.sorted.map(f => {
      englogBasicSchema.field(f, Types.STRING)
    })
    fieldsAppdet.sorted.map(f => {
      englogAppdetSchema.field(f, Types.STRING)
    })
    fieldsUrldet.sorted.map(f => {
      englogUrldetSchema.field(f, Types.STRING)
    })
    fieldsWifi.sorted.map(f => {
      englogWifiSchema.field(f, Types.STRING)
    })
    fieldsJcTest.sorted.map(f => {
      englogJcTestSchema.field(f, Types.STRING)
    })
  }

  def regist(): Unit = {
    tableEnv.connect(
      new Kafka()
        .version("0.11")
        .properties(kafkaConfluentProperties)
        .property("group.id", "flink_hadoop")
        .topic("englog_origin")
        .startFromGroupOffsets()
    ).withFormat(
      new Json()
        .deriveSchema()
    ).withSchema(
      englogOriginSchema
    ).inAppendMode(
    ).registerTableSource(
      tableSourceEnglogOrigin
    )

    tableEnv.connect(
      new Kafka()
        .version("0.11")
        .properties(kafkaConfluentProperties)
        .topic("englog_basic_new")
        .sinkPartitionerFixed()
    ).withFormat(
      new Json()
        .deriveSchema()
    ).withSchema(
      englogBasicSchema
    ).inAppendMode(
    ).registerTableSink(
      tableSinkEnglogBasic
    )

    tableEnv.connect(
      new Kafka()
        .version("0.11")
        .properties(kafkaConfluentProperties)
        .topic("englog_appdet_new")
        .sinkPartitionerFixed()
    ).withFormat(
      new Json()
        .deriveSchema()
    ).withSchema(
      englogAppdetSchema
    ).inAppendMode(
    ).registerTableSink(
      tableSinkEnglogAppdet
    )

    tableEnv.connect(
      new Kafka()
        .version("0.11")
        .properties(kafkaConfluentProperties)
        .topic("englog_urldet_new")
        .sinkPartitionerFixed()
    ).withFormat(
      new Json()
        .deriveSchema()
    ).withSchema(
      englogUrldetSchema
    ).inAppendMode(
    ).registerTableSink(
      tableSinkEnglogUrldet
    )

    tableEnv.connect(
      new Kafka()
        .version("0.11")
        .properties(kafkaConfluentProperties)
        .topic("englog_wifi_new")
        .sinkPartitionerFixed()
    ).withFormat(
      new Json()
        .deriveSchema()
    ).withSchema(
      englogWifiSchema
    ).inAppendMode(
    ).registerTableSink(
      tableSinkEnglogWifi
    )

    tableEnv.connect(
      new Kafka()
        .version("0.11")
        .properties(kafkaConfluentProperties)
        .topic("jc_test")
        .sinkPartitionerFixed()
    ).withFormat(
      new Json()
        .deriveSchema()
    ).withSchema(
      englogJcTestSchema
    ).inAppendMode(
    ).registerTableSink(
      tableSinkJcTest
    )
  }

  def transform(): Unit = {
    val basicSql = s"select ${fieldsEnglogHead.mkString(",")} from $tableSourceEnglogOrigin where d_log is null"
      .replaceFirst("system", "`system`")
    val appdetSql = s"select ${fieldsEnglogHead.mkString(",")}, d_log from $tableSourceEnglogOrigin where d_log is not null"
      .replaceFirst("system", "`system`")
    val urldetSql = s"select ${fieldsEnglogHead.mkString(",")}, urldet_log from $tableSourceEnglogOrigin where urldet_log is not null"
      .replaceFirst("system", "`system`")
    val wifiSql = s"select ${fieldsEnglogHead.mkString(",")}, wifi_log from $tableSourceEnglogOrigin where wifi_log is not null"
      .replaceFirst("system", "`system`")
    val jcTestSql = s"select ${fieldsEnglogHead.mkString(",")}, urldet_log from $tableSourceEnglogOrigin where urldet_log is not null"
      .replaceFirst("system", "`system`")

    val streamBasic: DataStream[Row] = tableEnv.sqlQuery(basicSql).toAppendStream[Row]
    val streamAppdet: DataStream[Row] = tableEnv.sqlQuery(appdetSql).toAppendStream[Row]
    val streamUrldet: DataStream[Row] = tableEnv.sqlQuery(urldetSql).toAppendStream[Row]
    val streamWifi: DataStream[Row] = tableEnv.sqlQuery(wifiSql).toAppendStream[Row]
    val streamJcTest: DataStream[Row] = tableEnv.sqlQuery(jcTestSql).toAppendStream[Row]

    val basic: DataStream[EnglogBasic] = streamBasic.flatMap(new FlatMapFunction[Row, EnglogBasic] {
      override def flatMap(t: Row, collector: Collector[EnglogBasic]): Unit = {
        val bsc = new EnglogBasic()
        bsc.put("d_len", "0")
        fieldsEnglogHead.indices.foreach(i => {
          bsc.put(fieldsEnglogHead.apply(i), t.getField(i).asInstanceOf[String])
        })
        collector.collect(bsc)
      }
    })

    val basicApp: DataStream[EnglogBasic] = streamAppdet.flatMap(new FlatMapFunction[Row, EnglogBasic] {
      override def flatMap(t: Row, collector: Collector[EnglogBasic]): Unit = {
        val bsc = new EnglogBasic()
        fieldsEnglogHead.indices.foreach(i => {
            bsc.put(fieldsEnglogHead.apply(i), t.getField(i).asInstanceOf[String])
        })
        val d_log = t.getField(t.getArity - 1).asInstanceOf[JList[JMap[String, String]]]
        bsc.put("d_len", d_log.size().toString)
        collector.collect(bsc)
      }
    })

    val appdet: DataStream[EnglogAppdet] = streamAppdet.flatMap(new FlatMapFunction[Row, EnglogAppdet] {
      override def flatMap(t: Row, collector: Collector[EnglogAppdet]): Unit = {
        var head:Map[String, String] = Map()
        fieldsEnglogHead.indices.foreach(i => {
          head += (fieldsEnglogHead.apply(i) -> t.getField(i).asInstanceOf[String])
        })
        val d_log = t.getField(t.getArity - 1).asInstanceOf[JList[JMap[String, String]]]
        d_log.map(data => {
          val app = new EnglogAppdet()
          head.foreach(kv => {
            app.put(kv._1, kv._2)
          })
          data.foreach(kv => {
            app.put(kv._1, kv._2)
          })
          collector.collect(app)
      })
      }
    })

    val urldet: DataStream[EnglogUrldet] = streamUrldet.flatMap(new FlatMapFunction[Row, EnglogUrldet] {
      override def flatMap(t: Row, collector: Collector[EnglogUrldet]): Unit = {
        var head:Map[String, String] = Map()
        fieldsEnglogHead.indices.foreach(i => {
          head += (fieldsEnglogHead.apply(i) -> t.getField(i).asInstanceOf[String])
        })
        val url_log = t.getField(t.getArity - 1).asInstanceOf[JList[JMap[String, String]]]
        url_log.map(data => {
          val url = new EnglogUrldet()
          head.foreach(kv => {
            url.put(kv._1, kv._2)
          })
          data.foreach(kv => {
            url.put(kv._1, kv._2)
          })
          collector.collect(url)
        })
      }
    })

    val wifi: DataStream[EnglogWifi] = streamWifi.flatMap(new FlatMapFunction[Row, EnglogWifi] {
      override def flatMap(t: Row, collector: Collector[EnglogWifi]): Unit = {
        var head:Map[String, String] = Map()
        fieldsEnglogHead.indices.foreach(i => {
          head += (fieldsEnglogHead.apply(i) -> t.getField(i).asInstanceOf[String])
        })
        val wifi_log = t.getField(t.getArity - 1).asInstanceOf[JList[JMap[String, String]]]
        wifi_log.map(data => {
          val wf = new EnglogWifi()
          head.foreach(kv => {
            wf.put(kv._1, kv._2)
          })
          data.foreach(kv => {
            wf.put(kv._1, kv._2)
          })
          collector.collect(wf)
        })
      }
    })

    val jctest: DataStream[JctestUrldet] = streamUrldet.flatMap(new FlatMapFunction[Row, JctestUrldet] {
      override def flatMap(t: Row, collector: Collector[JctestUrldet]): Unit = {
        var head:Map[String, String] = Map()
        fieldsEnglogHead.indices.foreach(i => {
          head += (fieldsEnglogHead.apply(i) -> t.getField(i).asInstanceOf[String])
        })
        val url_log = t.getField(t.getArity - 1).asInstanceOf[JList[JMap[String, String]]]
        url_log.map(data => {
          val url = new JctestUrldet()
          head.foreach(kv => {
            url.put(kv._1, kv._2)
          })
          data.foreach(kv => {
            url.put(kv._1, kv._2)
          })
          collector.collect(url)
        })
      }
    })

    tableEnv.fromDataStream(basic).insertInto(tableSinkEnglogBasic)
    tableEnv.fromDataStream(basicApp).insertInto(tableSinkEnglogBasic)
    tableEnv.fromDataStream(appdet).insertInto(tableSinkEnglogAppdet)
    tableEnv.fromDataStream(urldet).insertInto(tableSinkEnglogUrldet)
    tableEnv.fromDataStream(wifi).insertInto(tableSinkEnglogWifi)
    tableEnv.fromDataStream(jctest).insertInto(tableSinkJcTest)
  }

  def save(): Unit = {
  }
}
