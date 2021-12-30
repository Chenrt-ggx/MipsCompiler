package MipsObjectCode.InstructionSet;

public abstract class AbstractInstruction {
    public static AbstractOther getComment(String comment) {
        return new AbstractOther("#", -1, -1, comment, (x, y, z, w) -> x + " " + y);
    }

    public static AbstractOther getSyscall() {
        return new AbstractOther("syscall", -1, -1, null, (x, y, z, w) -> x);
    }

    public static AbstractOther getLui(int dst, int imm) {
        return new AbstractOther("lui", dst, imm, null, (x, y, z, w) -> x + " $" + z + ", " + w);
    }

    public static AbstractOther getTag(String tag) {
        return new AbstractOther(tag + ":", -1, -1, null, (x, y, z, w) -> x);
    }

    public static AbstractOther getJump(String tag) {
        return new AbstractOther("j", -1, -1, tag, (x, y, z, w) -> x + " " + y);
    }

    public static AbstractOther getLoadTag(int dst, String tag) {
        return new AbstractOther("la", dst, -1, tag, (x, y, z, w) -> x + " $" + z + ", " + y);
    }

    public static AbstractOther getJumpLink(String tag) {
        return new AbstractOther("jal", -1, -1, tag, (x, y, z, w) -> x + " " + y);
    }

    public static AbstractOther getJumpRegister(int dst) {
        return new AbstractOther("jr", dst, -1, null, (x, y, z, w) -> x + " $" + z);
    }

    public static AbstractBranch getBne(int src1, int src2, String tag) {
        return new AbstractBranch("bne", src1, src2, tag);
    }

    public static AbstractBranch getBeq(int src1, int src2, String tag) {
        return new AbstractBranch("beq", src1, src2, tag);
    }

    public static AbstractBranch getBgez(int src, String tag) {
        return new AbstractBranch("bgez", src, tag);
    }

    public static AbstractBranch getBgtz(int src, String tag) {
        return new AbstractBranch("bgtz", src, tag);
    }

    public static AbstractBranch getBlez(int src, String tag) {
        return new AbstractBranch("blez", src, tag);
    }

    public static AbstractBranch getBltz(int src, String tag) {
        return new AbstractBranch("bltz", src, tag);
    }

    public static AbstractRegRegCal getAddu(int dst, int src1, int src2) {
        return new AbstractRegRegCal("addu", dst, src1, src2);
    }

    public static AbstractRegRegCal getSubu(int dst, int src1, int src2) {
        return new AbstractRegRegCal("subu", dst, src1, src2);
    }

    public static AbstractRegRegCal getSlt(int dst, int src1, int src2) {
        return new AbstractRegRegCal("slt", dst, src1, src2);
    }

    public static AbstractRegRegCal getSltu(int dst, int src1, int src2) {
        return new AbstractRegRegCal("sltu", dst, src1, src2);
    }

    public static AbstractRegRegCal getXor(int dst, int src1, int src2) {
        return new AbstractRegRegCal("xor", dst, src1, src2);
    }

    public static AbstractRegImmCal getAddiu(int dst, int src, int imm) {
        return new AbstractRegImmCal("addiu", dst, src, imm);
    }

    public static AbstractRegImmCal getSll(int dst, int src, int imm) {
        return new AbstractRegImmCal("sll", dst, src, imm);
    }

    public static AbstractRegImmCal getSrl(int dst, int src, int imm) {
        return new AbstractRegImmCal("srl", dst, src, imm);
    }

    public static AbstractRegImmCal getSra(int dst, int src, int imm) {
        return new AbstractRegImmCal("sra", dst, src, imm);
    }

    public static AbstractRegImmCal getSlti(int dst, int src, int imm) {
        return new AbstractRegImmCal("slti", dst, src, imm);
    }

    public static AbstractRegImmCal getSltiu(int dst, int src, int imm) {
        return new AbstractRegImmCal("sltiu", dst, src, imm);
    }

    public static AbstractRegImmCal getOri(int dst, int src, int imm) {
        return new AbstractRegImmCal("ori", dst, src, imm);
    }

    public static AbstractRegImmCal getXori(int dst, int src, int imm) {
        return new AbstractRegImmCal("xori", dst, src, imm);
    }

    public static AbstractRegImmCal getSllv(int dst, int src, int imm) {
        return new AbstractRegImmCal("sllv", dst, src, imm);
    }

    public static AbstractMulDiv getMult(int src1, int src2) {
        return new AbstractMulDiv("mult", src1, src2);
    }

    public static AbstractMulDiv getDiv(int src1, int src2) {
        return new AbstractMulDiv("div", src1, src2);
    }

    public static AbstractMove getMfhi(int dst) {
        return new AbstractMove("mfhi", dst, -1);
    }

    public static AbstractMove getMflo(int dst) {
        return new AbstractMove("mflo", dst, -1);
    }

    public static AbstractLoadStore getLoad(int reg, int offset, int addr) {
        return new AbstractLoadStore("lw", reg, offset, addr);
    }

    public static AbstractLoadStore getStore(int reg, int offset, int addr) {
        return new AbstractLoadStore("sw", reg, offset, addr);
    }

    protected int indent;
    protected final String name;

    public AbstractInstruction(String name) {
        this.name = name;
        this.indent = 4;
    }

    public void setIndent(int indent) {
        this.indent = indent;
    }

    public int getIndent() {
        return indent;
    }

    public String getName() {
        return name;
    }
}