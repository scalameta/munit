package munit

import scala.concurrent.Promise
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext
import java.util.concurrent.ScheduledExecutorService

class AsyncFixtureSuite extends BaseSuite {
  case class PromiseWrapper(name: String, promise: Promise[_])
  override def munitValueTransforms: List[ValueTransform] =
    super.munitValueTransforms ++ List(
      new ValueTransform(
        "PromiseWrapper",
        { case p: PromiseWrapper =>
          p.promise.future
        }
      )
    )
  class ScheduledMessage() extends AnyFixture[String]("AsyncFixture") {
    val sh: ScheduledExecutorService =
      Executors.newSingleThreadScheduledExecutor()
    private var didBeforeAllEvaluateAsync = false
    private var promise = Promise[String]()
    private val timeout = 20
    def apply(): String = promise.future.value.get.get
    override def beforeAll(): PromiseWrapper = {
      val setBeforeAllBit = Promise[Unit]()
      sh.schedule[Unit](
        () => {
          didBeforeAllEvaluateAsync = true
          setBeforeAllBit.success(())
        },
        timeout,
        TimeUnit.MILLISECONDS
      )
      PromiseWrapper("beforeAll", setBeforeAllBit)
    }
    override def beforeEach(context: BeforeEach): PromiseWrapper = {
      assertEquals(
        promise.future.value,
        None,
        "promise did not get reset from afterEach"
      )
      assert(
        didBeforeAllEvaluateAsync,
        "beforeAll promise did not complete yet"
      )
      sh.schedule[Unit](
        () => promise.success(s"beforeEach-${context.test.name}"),
        timeout,
        TimeUnit.MILLISECONDS
      )
      PromiseWrapper("beforeEach", promise)
    }
    override def afterEach(context: AfterEach): PromiseWrapper = {
      val resetPromise = Promise[Unit]()
      sh.schedule[Unit](
        () => {
          promise = Promise[String]()
          resetPromise.success(())
        },
        timeout,
        TimeUnit.MILLISECONDS
      )
      PromiseWrapper("afterEach", resetPromise)
    }
    override def afterAll(): PromiseWrapper = {
      val shutdownPromise = Promise[Unit]()
      ExecutionContext.global.execute(() => {
        Thread.sleep(timeout)
        val runningJobs = sh.shutdownNow()
        assert(runningJobs.isEmpty(), runningJobs)
        shutdownPromise.success(())
      })
      PromiseWrapper("afterAll", shutdownPromise)
    }
  }
  val message = new ScheduledMessage()
  val latest: Fixture[Unit] = new Fixture[Unit]("latest") {
    def apply(): Unit = ()
    override def afterAll(): Unit = {
      assert(
        message.sh.isShutdown(),
        "message.afterAll did not complete yet. " +
          "We may want to remove this assertion in the future if we allow fixtures to load in parallel."
      )
    }
  }

  override def munitFixtures: Seq[AnyFixture[_]] = List(latest, message)

  1.to(3).foreach { i =>
    test(s"test-$i") {
      assertEquals(message(), s"beforeEach-test-$i")
    }
  }
}
