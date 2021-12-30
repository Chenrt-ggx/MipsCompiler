package MipsObjectCode;

import Global.Pair;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.math.BigInteger;
import IntermediateCode.Operands.AbstractVariable;
import IntermediateCode.Operands.AbstractLeftValue;
import MipsObjectCode.InstructionSet.AbstractInstruction;

public class MipsMulDivOptimize {
    private static class MulExtract {
        private final int steps;
        private final int source;
        private final ArrayList<Pair<Boolean, Integer>> items;

        private MulExtract(int... values) {
            int optimizeValue = 0;
            items = new ArrayList<>();
            for (int i : values) {
                if (i >= 0) {
                    optimizeValue += 1 << i;
                    items.add(new Pair<>(true, i & Integer.MAX_VALUE));
                }
                else {
                    optimizeValue -= 1 << (i & Integer.MAX_VALUE);
                    items.add(new Pair<>(false, i & Integer.MAX_VALUE));
                }
            }
            source = optimizeValue;
            int count = items.get(0).getKey() || items.get(0).getValue() == 0 ? 1 : 2;
            for (int i = 1; i < items.size(); i++) {
                count += items.get(i).getValue() == 0 ? 1 : 2;
            }
            steps = count;
        }

        private int getSteps() {
            return steps;
        }

        private int getSource() {
            return source;
        }

        private boolean isBetter() {
            int base = 6;
            if ((source & 0xffff) == 0 || (Short.MIN_VALUE <= source
                    && source <= Short.MAX_VALUE - Short.MIN_VALUE)) {
                base--;
            }
            return steps < base;
        }

        private void solve(int dst, int src, ArrayList<AbstractInstruction> objectCode) {
            if (items.size() == 1) {
                objectCode.add(AbstractInstruction.getSll(dst, src, items.get(0).getValue()));
                if (!items.get(0).getKey()) {
                    objectCode.add(AbstractInstruction.getSubu(dst, MipsAssembly.zero, dst));
                }
                return;
            }
            objectCode.add(AbstractInstruction.getSll(MipsAssembly.v1, src, items.get(0).getValue()));
            if (!items.get(0).getKey()) {
                objectCode.add(AbstractInstruction.getSubu(
                        MipsAssembly.v1, MipsAssembly.zero, MipsAssembly.v1));
            }
            for (int i = 1; i < items.size() - 1; i++) {
                if (items.get(i).getValue() == 0) {
                    objectCode.add(items.get(i).getKey() ?
                            AbstractInstruction.getAddu(MipsAssembly.v1, MipsAssembly.v1, src) :
                            AbstractInstruction.getSubu(MipsAssembly.v1, MipsAssembly.v1, src));
                }
                else {
                    objectCode.add(AbstractInstruction.getSll(MipsAssembly.at, src, items.get(i).getValue()));
                    objectCode.add(items.get(i).getKey() ?
                            AbstractInstruction.getAddu(MipsAssembly.v1, MipsAssembly.v1, MipsAssembly.at) :
                            AbstractInstruction.getSubu(MipsAssembly.v1, MipsAssembly.v1, MipsAssembly.at));
                }
            }
            if (items.get(items.size() - 1).getValue() == 0) {
                objectCode.add(items.get(items.size() - 1).getKey() ?
                        AbstractInstruction.getAddu(dst, MipsAssembly.v1, src) :
                        AbstractInstruction.getSubu(dst, MipsAssembly.v1, src));
            }
            else {
                objectCode.add(AbstractInstruction.getSll(
                        MipsAssembly.at, src, items.get(items.size() - 1).getValue()));
                objectCode.add(items.get(items.size() - 1).getKey() ?
                        AbstractInstruction.getAddu(dst, MipsAssembly.v1, MipsAssembly.at) :
                        AbstractInstruction.getSubu(dst, MipsAssembly.v1, MipsAssembly.at));
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("x * " + source + " = ");
            for (int i = 0; i < items.size(); i++) {
                if (i != 0 || !items.get(i).getKey()) {
                    builder.append(items.get(i).getKey() ? "+ " : "- ");
                }
                if (items.get(i).getValue() == 0) {
                    builder.append("x ");
                }
                else {
                    builder.append("(x << ").append(items.get(i).getValue()).append(") ");
                }
            }
            return builder.append("-> ").append(steps).toString();
        }
    }

    private static int shPosition;
    private static int leftOneBit;
    private static long magicValue;
    private static final HashMap<Integer, MulExtract> mulOptimizable;

    static {
        LinkedList<MulExtract> sourceList = new LinkedList<>();
        for (int i = 0; i < 32; i++) {
            sourceList.add(new MulExtract(i));
            sourceList.add(new MulExtract(i | 0x80000000));
            for (int j = 0; j < 32; j++) {
                sourceList.add(new MulExtract(i, j));
                sourceList.add(new MulExtract(i, j | 0x80000000));
                sourceList.add(new MulExtract(i | 0x80000000, j));
                sourceList.add(new MulExtract(i | 0x80000000, j | 0x80000000));
                for (int k = 0; k < 32; k++) {
                    sourceList.add(new MulExtract(i, j, k));
                    sourceList.add(new MulExtract(i, j, k | 0x80000000));
                    sourceList.add(new MulExtract(i, j | 0x80000000, k));
                    sourceList.add(new MulExtract(i, j | 0x80000000, k | 0x80000000));
                    sourceList.add(new MulExtract(i | 0x80000000, j, k));
                    sourceList.add(new MulExtract(i | 0x80000000, j, k | 0x80000000));
                    sourceList.add(new MulExtract(i | 0x80000000, j | 0x80000000, k));
                    sourceList.add(new MulExtract(i | 0x80000000, j | 0x80000000, k | 0x80000000));
                }
            }
        }
        mulOptimizable = new HashMap<>();
        sourceList.stream().filter(MulExtract::isBetter).forEach(i -> {
            if (!mulOptimizable.containsKey(i.getSource()) || i.getSteps() <
                    mulOptimizable.get(i.getSource()).getSteps()) {
                mulOptimizable.put(i.getSource(), i);
            }
        });
    }

    protected static boolean canOptimizeMul(int value) {
        return mulOptimizable.containsKey(value);
    }

    protected static void mulCalOptimize(AbstractLeftValue dst, AbstractVariable src, int value
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        int srcRegister = src.loadToRegister(assembly);
        int dstRegister = dst.appointRegister(assembly);
        mulOptimizable.get(value).solve(dstRegister, srcRegister, objectCode);
    }

    private static void getMagicValue(BigInteger divImm) {
        leftOneBit = 0;
        while (divImm.compareTo(BigInteger.valueOf(1).shiftLeft(leftOneBit)) > 0) {
            leftOneBit++;
        }
        shPosition = leftOneBit;
        long lowValue = BigInteger.valueOf(1).shiftLeft(32 + leftOneBit).divide(divImm).longValue();
        long highValue = BigInteger.valueOf(1).shiftLeft(32 + leftOneBit).add(BigInteger.valueOf(1)
                .shiftLeft(1 + leftOneBit)).divide(divImm).longValue();
        while (highValue >> 1 > lowValue >> 1 && shPosition > 0) {
            highValue >>= 1;
            lowValue >>= 1;
            shPosition--;
        }
        magicValue = highValue;
    }

    private static void divTemplate(int dst, int src, ArrayList<AbstractInstruction> objectCode) {
        if (magicValue < Integer.MAX_VALUE) {
            MipsCalTemplate.initTemplate(MipsAssembly.at, (int)magicValue, objectCode);
            objectCode.add(AbstractInstruction.getMult(MipsAssembly.at, src));
            objectCode.add(AbstractInstruction.getMfhi(MipsAssembly.at));
            objectCode.add(AbstractInstruction.getSra(MipsAssembly.v1, MipsAssembly.at, shPosition));
        }
        else {
            MipsCalTemplate.initTemplate(MipsAssembly.at, (int)(magicValue - (1L << 32)), objectCode);
            objectCode.add(AbstractInstruction.getMult(MipsAssembly.at, src));
            objectCode.add(AbstractInstruction.getMfhi(MipsAssembly.at));
            objectCode.add(AbstractInstruction.getAddu(MipsAssembly.v1, src, MipsAssembly.at));
            objectCode.add(AbstractInstruction.getSra(MipsAssembly.v1, MipsAssembly.v1, shPosition));
        }
        objectCode.add(AbstractInstruction.getSlt(MipsAssembly.at, src, MipsAssembly.zero));
        objectCode.add(AbstractInstruction.getAddu(dst, MipsAssembly.v1, MipsAssembly.at));
    }

    protected static void divCalOptimize(AbstractLeftValue dst, AbstractVariable src, int value
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        getMagicValue(BigInteger.valueOf(value).abs());
        int srcRegister = src.loadToRegister(assembly);
        int dstRegister = dst.appointRegister(assembly);
        if (Math.abs(value) == 1) {
            MipsCalTemplate.initTemplate(dstRegister, srcRegister, objectCode);
        }
        else if (Integer.bitCount(Math.abs(value)) == 1) {
            objectCode.add(AbstractInstruction.getSra(MipsAssembly.at, srcRegister, leftOneBit - 1));
            if (leftOneBit != 32) {
                objectCode.add(AbstractInstruction.getSrl(MipsAssembly.at
                        , MipsAssembly.at, 32 - leftOneBit));
            }
            objectCode.add(AbstractInstruction.getAddu(dstRegister, srcRegister, MipsAssembly.at));
            objectCode.add(AbstractInstruction.getSra(dstRegister, dstRegister, leftOneBit));
        }
        else {
            divTemplate(dstRegister, srcRegister, objectCode);
        }
        if (value < 0) {
            objectCode.add(AbstractInstruction.getSubu(dstRegister, MipsAssembly.zero, dstRegister));
        }
    }

    protected static void modCalOptimize(AbstractLeftValue dst, AbstractVariable src, int value
            , ArrayList<AbstractInstruction> objectCode, MipsAssembly assembly) {
        getMagicValue(BigInteger.valueOf(value).abs());
        int srcRegister = src.loadToRegister(assembly);
        int dstRegister = dst.appointRegister(assembly);
        if (Math.abs(value) == 1) {
            MipsCalTemplate.initTemplate(dstRegister, MipsAssembly.zero, objectCode);
        }
        else if (Integer.bitCount(Math.abs(value)) == 1) {
            objectCode.add(AbstractInstruction.getSra(MipsAssembly.at, srcRegister, leftOneBit - 1));
            if (leftOneBit != 32) {
                objectCode.add(AbstractInstruction.getSrl(MipsAssembly.at
                        , MipsAssembly.at, 32 - leftOneBit));
            }
            objectCode.add(AbstractInstruction.getAddu(MipsAssembly.at, srcRegister, MipsAssembly.at));
            objectCode.add(AbstractInstruction.getSra(MipsAssembly.at, MipsAssembly.at, leftOneBit));
            if (value < 0) {
                objectCode.add(AbstractInstruction.getSubu(
                        MipsAssembly.at, MipsAssembly.zero, MipsAssembly.at));
            }
            objectCode.add(AbstractInstruction.getSll(MipsAssembly.at, MipsAssembly.at, leftOneBit));
            objectCode.add(AbstractInstruction.getSubu(dstRegister, srcRegister, MipsAssembly.at));
        }
        else {
            divTemplate(MipsAssembly.v1, srcRegister, objectCode);
            if (value < 0) {
                objectCode.add(AbstractInstruction.getSubu(
                        MipsAssembly.v1, MipsAssembly.zero, MipsAssembly.v1));
            }
            MipsCalTemplate.initTemplate(MipsAssembly.at, value, objectCode);
            objectCode.add(AbstractInstruction.getMult(MipsAssembly.at, MipsAssembly.v1));
            objectCode.add(AbstractInstruction.getMflo(MipsAssembly.v1));
            objectCode.add(AbstractInstruction.getSubu(dstRegister, srcRegister, MipsAssembly.v1));
        }
    }
}