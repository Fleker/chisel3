// SPDX-License-Identifier: Apache-2.0

package chiselTests

import scala.collection.immutable.ListMap

// Keep Chisel._ separate from chisel3._ below
object CompatibilityComponents {
  import Chisel._
  import Chisel3Components._

  class ChiselBundle extends Bundle {
    val a = UInt(width = 32)
    val b = UInt(width = 32).flip
  }
  class ChiselRecord extends Record {
    val elements = ListMap("a" -> UInt(width = 32), "b" -> UInt(width = 32).flip)
    override def cloneType: this.type = (new ChiselRecord).asInstanceOf[this.type]
  }

  abstract class ChiselDriverModule(_io: => Record) extends Module {
    val io = _io
    io.elements("a").asUInt := UInt(123)
    assert(io.elements("b").asUInt === UInt(123))
  }
  abstract class ChiselPassthroughModule(_io: => Record) extends Module {
    val io = _io
    io.elements("b").asUInt := io.elements("a").asUInt
  }

  class ChiselBundleModuleA extends ChiselDriverModule(new ChiselBundle)
  class ChiselBundleModuleB extends ChiselPassthroughModule((new ChiselBundle).flip)
  class ChiselRecordModuleA extends ChiselDriverModule(new ChiselRecord)
  class ChiselRecordModuleB extends ChiselPassthroughModule((new ChiselRecord).flip)

  class ChiselModuleChisel3BundleA extends ChiselDriverModule(new Chisel3Bundle)
  class ChiselModuleChisel3BundleB extends ChiselPassthroughModule((new Chisel3Bundle).flip)
  class ChiselModuleChisel3RecordA extends ChiselDriverModule(new Chisel3Record)
  class ChiselModuleChisel3RecordB extends ChiselPassthroughModule((new Chisel3Record).flip)
}

object Chisel3Components {
  import chisel3._
  import CompatibilityComponents._

  class Chisel3Bundle extends Bundle {
    val a = Output(UInt(32.W))
    val b = Input(UInt(32.W))
  }

  class Chisel3Record extends Record {
    val elements = ListMap("a" -> Output(UInt(32.W)), "b" -> Input(UInt(32.W)))
    override def cloneType: this.type = (new Chisel3Record).asInstanceOf[this.type]
  }

  abstract class Chisel3DriverModule(_io: => Record) extends Module {
    val io = IO(_io)
    io.elements("a").asUInt := 123.U
    assert(io.elements("b").asUInt === 123.U)
  }
  abstract class Chisel3PassthroughModule(_io: => Record) extends Module {
    val io = IO(_io)
    io.elements("b").asUInt := io.elements("a").asUInt
  }

  class Chisel3BundleModuleA extends Chisel3DriverModule(new Chisel3Bundle)
  class Chisel3BundleModuleB extends Chisel3PassthroughModule(Flipped(new Chisel3Bundle))
  class Chisel3RecordModuleA extends Chisel3DriverModule(new Chisel3Record)
  class Chisel3RecordModuleB extends Chisel3PassthroughModule(Flipped(new Chisel3Record))

  class Chisel3ModuleChiselBundleA extends Chisel3DriverModule(new ChiselBundle)
  class Chisel3ModuleChiselBundleB extends Chisel3PassthroughModule(Flipped(new ChiselBundle))
  class Chisel3ModuleChiselRecordA extends Chisel3DriverModule(new ChiselRecord)
  class Chisel3ModuleChiselRecordB extends Chisel3PassthroughModule(Flipped(new ChiselRecord))
}

class CompatibiltyInteroperabilitySpec extends ChiselFlatSpec {

  "Modules defined in the Chisel._" should "successfully bulk connect in chisel3._" in {
    import chisel3._
    import chisel3.testers.BasicTester
    import CompatibilityComponents._

    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselBundleModuleA)
      val b = Module(new ChiselBundleModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselRecordModuleA)
      val b = Module(new ChiselRecordModuleB)
      b.io <> a.io
      stop()
    })
  }

  "Moduless defined in the chisel3._" should "successfully bulk connect in Chisel._" in {
    import Chisel._
    import chisel3.testers.BasicTester
    import Chisel3Components._

    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3BundleModuleA)
      val b = Module(new Chisel3BundleModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3RecordModuleA)
      val b = Module(new Chisel3RecordModuleB)
      b.io <> a.io
      stop()
    })
  }


  "Bundles defined in Chisel._" should "work in chisel3._ Modules" in {
    import chisel3._
    import chisel3.testers.BasicTester
    import Chisel3Components._

    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3ModuleChiselBundleA)
      val b = Module(new Chisel3ModuleChiselBundleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3ModuleChiselRecordA)
      val b = Module(new Chisel3ModuleChiselRecordB)
      b.io <> a.io
      stop()
    })
  }

  "Bundles defined in chisel3._" should "work in Chisel._ Modules" in {
    import chisel3._
    import chisel3.testers.BasicTester
    import CompatibilityComponents._

    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselModuleChisel3BundleA)
      val b = Module(new ChiselModuleChisel3BundleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselModuleChisel3RecordA)
      val b = Module(new ChiselModuleChisel3RecordB)
      b.io <> a.io
      stop()
    })
  }


  "Similar Bundles defined in the chisel3._ and Chisel._" should
      "successfully bulk connect in chisel3._" in {
    import chisel3._
    import chisel3.testers.BasicTester
    import Chisel3Components._
    import CompatibilityComponents._

    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselBundleModuleA)
      val b = Module(new Chisel3BundleModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3BundleModuleA)
      val b = Module(new ChiselBundleModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselRecordModuleA)
      val b = Module(new Chisel3RecordModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3RecordModuleA)
      val b = Module(new ChiselRecordModuleB)
      b.io <> a.io
      stop()
    })
  }
  they should "successfully bulk connect in Chisel._" in {
    import Chisel._
    import chisel3.testers.BasicTester
    import Chisel3Components._
    import CompatibilityComponents._

    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselBundleModuleA)
      val b = Module(new Chisel3BundleModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3BundleModuleA)
      val b = Module(new ChiselBundleModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new ChiselRecordModuleA)
      val b = Module(new Chisel3RecordModuleB)
      b.io <> a.io
      stop()
    })
    assertTesterPasses(new BasicTester {
      val a = Module(new Chisel3RecordModuleA)
      val b = Module(new ChiselRecordModuleB)
      b.io <> a.io
      stop()
    })
  }

  "An instance of a chisel3.Module inside a Chisel.Module" should "have its inputs invalidated" in {
    compile {
      import Chisel._
      new Module {
        val io = new Bundle {
          val in = UInt(INPUT, width = 32)
          val cond = Bool(INPUT)
          val out = UInt(OUTPUT, width = 32)
        }
        val children = Seq(Module(new PassthroughModule),
                           Module(new PassthroughMultiIOModule),
                           Module(new PassthroughRawModule))
        io.out := children.map(_.io.out).reduce(_ + _)
        children.foreach { child =>
          when (io.cond) {
            child.io.in := io.in
          }
        }
      }
    }
  }

  "Compatibility Modules" should "have Bool as their reset type" in {
    compile {
      import Chisel._
      class Intf extends Bundle {
        val in = Bool(INPUT)
        val en = Bool(INPUT)
        val out = Bool(OUTPUT)
      }
      class Child extends Module {
        val io = new Intf
        io.out := Mux(io.en, io.in, reset)
      }
      new Module {
        val io = new Intf
        val child = Module(new Child)
        io <> child.io
      }
    }
  }

  "Compatibility Modules" should "be instantiable inside chisel3 Modules" in {
    compile {
      object Compat {
        import Chisel._
        class Intf extends Bundle {
          val in = Input(UInt(8.W))
          val out = Output(UInt(8.W))
        }
        class OldMod extends Module {
          val io = IO(new Intf)
          io.out := Reg(next = io.in)
        }
      }
      import chisel3._
      import Compat._
      new Module {
        val io = IO(new Intf)
        io <> Module(new Module {
          val io = IO(new Intf)
          val inst = Module(new OldMod)
          io <> inst.io
        }).io
      }
    }
  }

  "A chisel3 Bundle that instantiates a Chisel Bundle" should "bulk connect correctly" in {
    compile {
      object Compat {
        import Chisel._
        class BiDir extends Bundle {
          val a = Input(UInt(8.W))
          val b = Output(UInt(8.W))
        }
        class Struct extends Bundle {
          val a = UInt(8.W)
        }
      }
      import chisel3._
      import Compat._
      class Bar extends Bundle {
        val bidir1 = new BiDir
        val bidir2 = Flipped(new BiDir)
        val struct1 = Output(new Struct)
        val struct2 = Input(new Struct)
      }
      // Check every connection both ways to see that chisel3 <>'s commutativity holds
      class Child extends RawModule {
        val deq = IO(new Bar)
        val enq = IO(Flipped(new Bar))
        enq <> deq
        deq <> enq
      }
      new RawModule {
        val deq = IO(new Bar)
        val enq = IO(Flipped(new Bar))
        // Also important to check connections to child ports
        val c1 = Module(new Child)
        val c2 = Module(new Child)
        c1.enq <> enq
        enq <> c1.enq
        c2.enq <> c1.deq
        c1.deq <> c2.enq
        deq <> c2.deq
        c2.deq <> deq
      }
    }
  }

  "A unidirectional but flipped Bundle" should "bulk connect in import chisel3._ code correctly" in {
    object Compat {
      import Chisel._
      class MyBundle(extraFlip: Boolean) extends Bundle {
        private def maybeFlip[T <: Data](t: T): T = if (extraFlip) t.flip else t
        val foo = maybeFlip(new Bundle {
          val bar = UInt(INPUT, width = 8)
        })
      }
    }
    import chisel3._
    import Compat._
    class Top(extraFlip: Boolean) extends RawModule {
      val port = IO(new MyBundle(extraFlip))
      val wire = Wire(new MyBundle(extraFlip))
      port <> DontCare
      wire <> DontCare
      port <> wire
      wire <> port
      port.foo <> wire.foo
      wire.foo <> port.foo
    }
    compile(new Top(true))
    compile(new Top(false))
  }
}

