package flinkflow.flow

trait BaseFlow {
  def init(): Unit
  def regist(): Unit
  def transform(): Unit
  def save(): Unit
  def schedule(): Unit = {
    init()
    regist()
    transform()
    save()
  }
}
