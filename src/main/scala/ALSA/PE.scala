package ALSA

import chisel3._
import chisel3.util.{Decoupled, Enum, Queue}
class PE(NUM_PE_REGS: Int = 20,NUM_SHARED_REGS: Int = 8, REGS_BITWIDTH: Int = 32) extends Module{
  val io = IO(new Bundle{
    val input_instruction=Input(Decoupled(UInt(32.W)))
    //val input_instruction=(Input(UInt(32.W)) //todo surely I need less bits for the instruction
    val out_val = Output(UInt(2.W))
  })

  /* PE Registers */
  val pe_regs = Mem(NUM_PE_REGS, SInt(REGS_BITWIDTH.W))

  /* Shared PE Registers - used to communicate with neighbours */
  val shared_regs = Mem(NUM_SHARED_REGS, SInt(REGS_BITWIDTH.W))

  /* Instatiate SW ALU */
  val sw_alu = Module(new ALU_SW)

  /*
  sw_alu.io.database := pe_regs(2)
  sw_alu.io.query := pe_regs(3)
  sw_alu.io.gapi := pe_regs(4)
  sw_alu.io.gapd := pe_regs(5)
  sw_alu.io.MATCH := ???
  sw_alu.io.MISMATCH := ???
  sw_alu.io.n := pe_regs(8)
  sw_alu.io.w := pe_regs(9)
  sw_alu.io.nw := pe_regs(10)
  pe_regs(11) := sw_alu.io.out_dir
  pe_regs(12) := sw_alu.io.out_max_pe
  pe_regs(8) := sw_alu.io.out_n

  shared_regs(0) := sw_alu.io.out_nw
  shared_regs(1) := sw_alu.io.out_w
*/
  /* FETCH Instruction */
  val instruction = Queue(io.input_instruction)
  val sIdle :: sDecode :: sCompute :: sStore :: Nil = Enum(UInt(), 4)
  val state_machine = Reg(init = sIdle)

  when(instruction.fire()){

  }

}
