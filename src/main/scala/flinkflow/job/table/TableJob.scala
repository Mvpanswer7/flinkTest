package flinkflow.job.table

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import flinkflow.flow.EnglogOriginSplit
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig

object TableJob {
  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.getConfig.enableForceAvro()
    env.getConfig.enableForceKryo()
    env.enableCheckpointing(10000)
    env.getCheckpointConfig.setFailOnCheckpointingErrors(false)
    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.AT_LEAST_ONCE)
    env.getCheckpointConfig.setCheckpointTimeout(120000)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(10)
    env.getCheckpointConfig.enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION)
    new EnglogOriginSplit(env).schedule()
    env.execute("Flink Table englog origin topic split")
  }
}
