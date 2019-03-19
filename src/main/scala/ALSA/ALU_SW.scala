package ALSA

import chisel3._
class ALU_SW extends Module{
  val io = IO(new Bundle{
    val start = Input(UInt(1.W))
    val query = Input(UInt(2.W))
    val database = Input(UInt(3.W)) //3bits to represent a different value when the PE needs to produce garbage
    val nw = Input(SInt(32.W)) //previous anti-diagonal
    val n = Input(SInt(32.W)) //north element
    val w = Input(SInt(32.W)) //west element

    //    val max = Input(SInt(32.W)) //maxScorePE
    val MATCH = Input(SInt(32.W)) //here the scoring system needs to be dimensioned
    val MISMATCH = Input(SInt(32.W))
    val gapd = Input(SInt(32.W))
    val gapi = Input(SInt(32.W))

    val out_dir = Output(UInt(2.W))
    val out_nw = Output(SInt(32.W))
    val out_n = Output(SInt(32.W))
    val out_w = Output(SInt(32.W))
    val out_max_pe = Output(SInt(32.W))
  })

  def val0 = 0.S(32.W)
  def val1 = io.nw + Mux(io.query === io.database, io.MATCH, io.MISMATCH)
  def val2 = io.n + io.gapd
  def val3 = io.w + io.gapi



  def dir = Mux(val1 > val0 && val1 >= val2 && val1 >= val3, (1 << 1).U(2.W),
    Mux(val2 > val0 && val2 >= val3, 1.U(2.W),
      Mux(val3 > val0, ((1 << 1) | (1 << 0)).U(2.W), 0.U(2.W))))

  def max_el = Mux(val1 > val0 && val1 >= val2 && val1 >= val3, val1,
    Mux(val2 > val0 && val2 >= val3, val2,
      Mux(val3 > val0, val3, val0)))

  //output the DATAAA!
  when(io.start === true.B){
    io.out_dir := dir
    io.out_max_pe := max_el//Mux(max_el > io.max, max_el, io.max)
    io.out_w := max_el
    io.out_n := max_el
    io.out_nw := io.n
  }otherwise{
    io.out_dir := 0.U
    io.out_max_pe := (-1).S//Mux(max_el > io.max, max_el, io.max)
    io.out_w := (-1).S
    io.out_n := (-1).S
    io.out_nw := (-1).S
  }

  //  printf("PE: in_query %d out_dir %d \n", io.query, io.out_dir)



  //  printf("1st: q %d - db %d - nw %d - n %d - w %d - MATCH %d - MISMATCH %d - gapd %d - gapi %d - val1 %d -  val2 %d - val3 %d - prop_n %d \n\n dir %d - max %d - out_n %d - out_w %d - out_nw %d \n*******\n", q, db, nw, n, w, MATCH, MISMATCH, gapd, gapi, val1, val2, val3, prop_n, dir, max, max, max, out_nw)

}
