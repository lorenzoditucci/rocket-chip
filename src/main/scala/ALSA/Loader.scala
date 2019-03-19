package ALSA

import Chisel.UInt
import chisel3._
import chisel3.util.Enum
import freechips.rocketchip.config.Parameters
import freechips.rocketchip.rocket.HellaCacheIO
import freechips.rocketchip.rocket.constants
class Loader(addr_width :Int, tag_width: Int)(implicit p:Parameters) extends Module{
  val io = IO(new Bundle{
    val start_request = Input(UInt(1.W))
    val busy = Output(UInt(1.W))

    val address = Input(UInt(addr_width.W))
    val tag = Input(UInt(tag_width.W))
    val mem = new HellaCacheIO
    val typ = Input(UInt(3.W))

    val out_val = Output(UInt(64.W)) //can handle up to 64
    val out_tag = Output(UInt(tag_width.W))
    val out_valid = Output(UInt(1.W))
  })

  /*
    Manage 1 request at a time
  */

  def driveDefaults() {
    io.mem.req.valid := false.B
    io.mem.req.bits.addr := 0.U
    io.mem.req.bits.tag := 0.U
    io.mem.req.bits.cmd := 0.U
    io.mem.req.bits.typ := 0.U
    io.mem.req.bits.phys := false.B
    io.mem.req.bits.data := 0.U

    //io.resp.valid := false.B
    //io.resp.bits.data := 0.U
    //io.resp.bits.rd := 0.U
  }

  val sIdle :: sRequest :: Nil = Enum(UInt(), 2)
  val state = Reg(init = sIdle)

  io.busy := false.B
  when(state === sIdle){
    io.busy := false.B
    when(io.start_request === true.B){
      state := sRequest
    }
  }.elsewhen(state === sRequest){
    io.busy := true.B

    io.mem.req.bits.addr := io.address
    io.mem.req.bits.tag := io.tag
    io.mem.req.bits.cmd := UInt("b00000") //integer load
    //io.mem.req.bits.typ := MT_D
    io.mem.req.bits.typ := io.typ // 32 o 64??
    io.mem.req.bits.phys := false.B //logical address
    io.mem.req.bits.data := 0.U //is a load
    io.mem.req.valid := true.B //todo this is not efficient at all, is there a way to do it faster?
    io.busy := true.B
    when(io.mem.req.fire()){
      state := sIdle
    }
  }

  /*
  when(io.start_request === true.B){
    io.mem.req.bits.addr := io.address
    io.mem.req.bits.tag := io.tag
    io.mem.req.bits.cmd := UInt("b00000") //integer load
    //io.mem.req.bits.typ := MT_D
    io.mem.req.bits.typ := io.typ // 32 o 64??
    io.mem.req.bits.phys := false.B //logical address
    io.mem.req.bits.data := 0.U //is a load
    io.mem.req.valid := true.B
    io.busy := true.B

    when(io.mem.req.fire()){
      io.busy := false.B
    }
  }*/


  /* The module should always be ready to handle data that is coming from Cache */
  io.out_valid := io.mem.resp.valid
  io.out_val := io.mem.resp.bits.data
  io.out_tag := io.mem.resp.bits.tag




}

object Loader extends App {
  chisel3.Driver.execute(args, () => new Loader(32, 7))
}
