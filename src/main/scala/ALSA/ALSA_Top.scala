package ALSA

import chisel3.{util, _}
import chisel3.util.{Queue, is, log2Up, switch}
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.tile.{HasCoreParameters, LazyRoCC, LazyRoCCModuleImp, OpcodeSet}

class ALSA_Top(opcodes: OpcodeSet, val num_ALUs: Int = 6, val numPEs: Int = 128)(implicit p: Parameters) extends LazyRoCC(opcodes) {
  override lazy val module = new ALSA_TopImpl(this)
}

class ALSA_TopImpl(outer: ALSA_Top)(implicit p:Parameters) extends LazyRoCCModuleImp(outer)
  with HasCoreParameters{

  def driveDefaults() {
    io.mem.req.valid := false.B
    io.mem.req.bits.addr := 0.U
    io.mem.req.bits.tag := 0.U
    io.mem.req.bits.cmd := 0.U
    io.mem.req.bits.typ := 0.U
    io.mem.req.bits.phys := false.B
    io.mem.req.bits.data := 0.U

    io.resp.valid := false.B
    io.resp.bits.data := 0.U
    io.resp.bits.rd := 0.U
  }


  val cmd = Queue(io.cmd)
  val funct = cmd.bits.inst.funct
  val rs1_val = cmd.bits.rs1
  val rs2_val = cmd.bits.rs2
  val hasRs1 = cmd.bits.inst.xs1
  val hasRs2z = cmd.bits.inst.xs2
  val needResp = cmd.bits.inst.xd

  val loader = Module(new Loader(xLen, dcacheReqTagBits))
  val regDoLoad = Reg(init = false.B)
  val regLoadedVal = Reg(init = 0.U(64.W))
  val regRequestTyp = Reg(init = io.mem.req.bits.typ)
  val regLoadAddr = Reg(init = rs1_val)
  val regLoadTag = Reg(init = io.mem.req.bits.tag)



  loader.io.start_request := regDoLoad
  loader.io.address := regLoadAddr
  loader.io.tag := regLoadTag
  regLoadedVal := loader.io.out_val
  loader.io.mem <> io.mem
  loader.io.typ := regRequestTyp



  /* External registers: each register has an entry for the PE */
  val regsExt_1 = Reg(init = Vec(Seq.fill(outer.numPEs)(0.U(32.W)))) //todo 32 bits?
  val regsExt_2 = Reg(init = Vec(Seq.fill(outer.numPEs)(0.U(32.W))))


  /*Accepted Instructions - 7 bits*/
  val LOAD = 0.U(7.W)
  val COMPUTE = 1.U(7.W)
  val STORE = 2.U(7.W)
  val LOAD_REG = 3.U(7.W)
  val LOAD_FIFO = 4.U(7.W)
  val COMPARE = ???
  val ADD = ???



  var regAddr = Reg(init = cmd.bits.rs1)
  var regDest = Reg(init = 0.U(6.W)) //todo change here
  var regNumElems = Reg(init = rs2_val)

  /*
  Keep track of what pes are busy, and if all are busy, or the necessary one is busy
  stall instruction fetch
  */
  val computeUnitBusy = Reg(init = 0.U(log2Up(outer.num_ALUs).W))

  val regBusy = Reg(init = false.B)

  io.busy := regBusy
  cmd.ready := ~regBusy //todo

  //todo logica per regbusy
  when(cmd.fire()){
    regBusy := true.B //todo

    switch(funct){
      is(LOAD){
        /*
        rs1 = address
        rs2 = destReg => (31,30) = which buffer, (30,0) = dest reg
        tag = 0 //one at a time per now
         */
        regDoLoad := true.B

      }

      /*is(COMPUTE){

      }

      is(STORE){

      }

      is(LOAD_REG){

      }

      is(LOAD_FIFO){
        //rs1= logical address
        //rs2= num_elems + destination reg (0 | 1)
        regAddr := rs1_val
        regDest := rs2_val(0)
        regNumElems := rs2_val(xLen -1, 1)
        //send requests


        //wait for requests

      }*/
    }
  }
}
