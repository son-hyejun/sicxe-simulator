// instruction에 따라 동작을 수행하는 메소드를 정의하는 클래스

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class InstLuncher {
    ResourceManager rMgr;
    private final Map<Integer, String> opcodeMap;
    private final Map<String, InstructionHandler> handlerMap;

    int targetAddr;
    String mnemonic;
    int prevPC;
    int prevLen;
    String curDev;
    boolean useDev;

    public InstLuncher(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
        this.opcodeMap = new HashMap<>();
        this.handlerMap = new HashMap<>();
        initInstructions();
        useDev = false;
    }

    private void initInstructions() {
        opcodeMap.put(0x00, "LDA");
        opcodeMap.put(0x0C, "STA");
        opcodeMap.put(0x28, "COMP");
        opcodeMap.put(0x30, "JEQ");
        opcodeMap.put(0x3C, "J");
        opcodeMap.put(0x48, "JSUB");  // +JSUB 포함
        opcodeMap.put(0x4C, "RSUB");
        opcodeMap.put(0x50, "LDCH");
        opcodeMap.put(0x54, "STCH");
        opcodeMap.put(0x68, "LDB");
        opcodeMap.put(0xB4, "CLEAR");
        opcodeMap.put(0x74, "LDT");  // +LDT 포함
        opcodeMap.put(0x10, "STX");  // +STX 포함
        opcodeMap.put(0xA0, "COMPR");
        opcodeMap.put(0xB8, "TIXR");
        opcodeMap.put(0xE0, "TD");
        opcodeMap.put(0xD8, "RD");
        opcodeMap.put(0xDC, "WD");
        opcodeMap.put(0x38, "JLT");
        opcodeMap.put(0x14, "STL");

        // 명령어 핸들러 등록
        handlerMap.put("LDA", this::handleLDA);
        handlerMap.put("STA", this::handleSTA);
        handlerMap.put("STL", this::handleSTL);
        handlerMap.put("COMP", this::handleCOMP);
        handlerMap.put("JEQ", this::handleJEQ);
        handlerMap.put("J", this::handleJ);
        handlerMap.put("JSUB", this::handleJSUB);
        handlerMap.put("RSUB", this::handleRSUB);
        handlerMap.put("LDCH", this::handleLDCH);
        handlerMap.put("STCH", this::handleSTCH);
        handlerMap.put("LDB", this::handleLDB);
        handlerMap.put("LDT", this::handleLDT);
        handlerMap.put("STX", this::handleSTX);
        handlerMap.put("CLEAR", this::handleCLEAR);
        handlerMap.put("COMPR", this::handleCOMPR);
        handlerMap.put("TIXR", this::handleTIXR);
        handlerMap.put("TD", this::handleTD);
        handlerMap.put("RD", this::handleRD);
        handlerMap.put("WD", this::handleWD);
        handlerMap.put("JLT", this::handleJLT);
    }

    public void executeNextInstruction() {
        int pc = rMgr.getRegister("PC");
        int byte1 = Byte.toUnsignedInt(rMgr.readByte(pc));
        int opcode = byte1 & 0xFC;

        prevPC = pc;

        String mnemonic = opcodeMap.get(opcode);
        if (mnemonic == null) {
            this.mnemonic = "";
            throw new RuntimeException(String.format("Unknown opcode: %02X at %06X", opcode, pc));
        }
        this.mnemonic = mnemonic;

        useDev = mnemonic.equals("TD") || mnemonic.equals("RD") || mnemonic.equals("WD");

        InstructionHandler handler = handlerMap.get(mnemonic);
        if (handler == null) {
            throw new UnsupportedOperationException("No handler for: " + mnemonic);
        }


        handler.execute(pc);
    }

    private void handleLDA(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        boolean immediate = info[1] == 1;
        int length = info[2];

        int value = immediate ? addr : rMgr.readWord(addr);
        rMgr.setRegister("A", value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleSTA(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int value = rMgr.getRegister("A");
        rMgr.writeWord(addr, value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleSTL(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int value = rMgr.getRegister("L");
        rMgr.writeWord(addr, value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleSTX(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int value = rMgr.getRegister("X");
        rMgr.writeWord(addr, value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleJSUB(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        rMgr.setRegister("L", pc + length);
        rMgr.setRegister("PC", addr);
    }

    private void handleCOMP(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        boolean immediate = info[1] == 1;
        int length = info[2];

        int a = rMgr.getRegister("A");
        int val = immediate ? addr : rMgr.readWord(addr);

        if (a == val) rMgr.setRegister("SW", 0);
        else if (a < val) rMgr.setRegister("SW", -1);
        else rMgr.setRegister("SW", 1);

        rMgr.setRegister("PC", pc + length);
    }

    private void handleJEQ(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int sw = rMgr.getRegister("SW");
        if (sw == 0) rMgr.setRegister("PC", addr);
        else rMgr.setRegister("PC", pc + length);
    }

    private void handleJ(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        rMgr.setRegister("PC", addr);
    }

    private void handleLDB(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        boolean immediate = info[1] == 1;
        int length = info[2];

        int value = immediate ? addr : rMgr.readWord(addr);
        rMgr.setRegister("B", value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleLDT(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        boolean immediate = info[1] == 1;
        int length = info[2];

        int value = immediate ? addr : rMgr.readWord(addr);
        rMgr.setRegister("T", value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleRSUB(int pc) {
        int byte2 = Byte.toUnsignedInt(rMgr.readByte(pc + 1));
        boolean e = (byte2 & 0x01) != 0;
        int length = e ? 4 : 3;

        int addr = rMgr.getRegister("L");
        rMgr.setRegister("PC", addr);
    }

    private void handleCLEAR(int pc) {
        int reg = Byte.toUnsignedInt(rMgr.readByte(pc + 1)) >> 4;
        rMgr.setRegister(reg, 0);
        rMgr.setRegister("PC", pc + 2);

        prevLen = 2;
        targetAddr = 0;
    }

    private void handleCOMPR(int pc) {
        int byte2 = Byte.toUnsignedInt(rMgr.readByte(pc + 1));
        int r1 = (byte2 >> 4) & 0x0F;
        int r2 = byte2 & 0x0F;
        int val1 = rMgr.getRegister(r1);
        int val2 = rMgr.getRegister(r2);

        if (val1 == val2) rMgr.setRegister("SW", 0);
        else if (val1 < val2) rMgr.setRegister("SW", -1);
        else rMgr.setRegister("SW", 1);

        rMgr.setRegister("PC", pc + 2);

        prevLen = 2;
        targetAddr = 0;
    }

    private void handleTIXR(int pc) {
        int r = Byte.toUnsignedInt(rMgr.readByte(pc + 1)) >> 4;
        int x = rMgr.getRegister("X") + 1;
        rMgr.setRegister("X", x);

        int val = rMgr.getRegister(r);
        if (x == val) rMgr.setRegister("SW", 0);
        else if (x < val) rMgr.setRegister("SW", -1);
        else rMgr.setRegister("SW", 1);

        rMgr.setRegister("PC", pc + 2);

        prevLen = 2;
        targetAddr = 0;
    }

    private void handleLDCH(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int value = Byte.toUnsignedInt(rMgr.readByte(addr));
        rMgr.setRegister("A", value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleSTCH(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int value = rMgr.getRegister("A") & 0xFF;
        rMgr.writeByte(addr, (byte) value);
        rMgr.setRegister("PC", pc + length);
    }

    private void handleTD(int pc) {
        int[] info = getTargetAddress(pc);
        int length = info[2];

        String devName = String.format("%02X", rMgr.readByte(info[0]) & 0xFF);
        rMgr.testDevice(devName);
        rMgr.setRegister("PC", pc + length);

        curDev = devName;
    }

    private void handleRD(int pc) {
        int[] info = getTargetAddress(pc);
        int length = info[2];

        String devName = String.format("%02X", rMgr.readByte(info[0]) & 0xFF);
        byte[] read = rMgr.readDevice(devName, 1);

        if (read != null) {
            int data = read[0];
            rMgr.setRegister("A", data);
        }

        rMgr.setRegister("PC", pc + length);

        curDev = devName;
    }

    private void handleWD(int pc) {
        int[] info = getTargetAddress(pc);
        int length = info[2];

        String devName = String.format("%02X", rMgr.readByte(info[0]) & 0xFF);
        int value = rMgr.getRegister("A") & 0xFF;
        byte data = rMgr.intToBytes(value)[2];

        rMgr.writeDevice(devName, new byte[]{data}, 1);
        rMgr.setRegister("PC", pc + length);

        curDev = devName;
    }

    private void handleJLT(int pc) {
        int[] info = getTargetAddress(pc);
        int addr = info[0];
        int length = info[2];

        int sw = rMgr.getRegister("SW");
        if (sw < 0) rMgr.setRegister("PC", addr);
        else rMgr.setRegister("PC", pc + length);
    }

    /**
     * @return [target address, immediate mode면 1, instruction 길이 (3 or 4)]
     */
    private int[] getTargetAddress(int pc) {
        int byte1 = rMgr.readByte(pc) & 0xFF;
        int byte2 = rMgr.readByte(pc + 1) & 0xFF;;
        int byte3 = rMgr.readByte(pc + 2) & 0xFF;;

        boolean n = (byte1 & 0x02) != 0;
        boolean i = (byte1 & 0x01) != 0;
        boolean x = (byte2 & 0x80) != 0;
        boolean b = (byte2 & 0x40) != 0;
        boolean p = (byte2 & 0x20) != 0;
        boolean e = (byte2 & 0x10) != 0;

        int disp = ((byte2 & 0x0F) << 8) | byte3;
        int target = 0;
        int formatLength = e ? 4 : 3;

        prevLen = formatLength;

        // Format 4일 경우 disp 확장
        if (e) {
            int byte4 = Byte.toUnsignedInt(rMgr.readByte(pc + 3));
            disp = (disp << 8) | byte4;
        }

        // 1. Immediate (n=0, i=1): disp 자체가 값
        if (!n && i) {
            targetAddr = disp;
            return new int[]{disp, 1, formatLength};
        }

        // 2. Simple or Indirect mode에서 target 계산
        if (!e && (b || p)) disp = signExtend(disp, 12);

        if (b) {
            target = rMgr.getRegister("B") + disp;
        } else if (p) {
            target = pc + formatLength + disp;
        } else {
            target = disp;
        }

        // 3. Indexed addressing (x 비트)
        if (!(n && !i) && x) {
            target += rMgr.getRegister("X");
        }

        // 4. Indirect (n=1, i=0): target 주소에 있는 word를 dereference
        if (n && !i) {
            int realAddr = rMgr.readWord(target);
            targetAddr = realAddr;

            return new int[]{realAddr, 0, formatLength};
        }

        targetAddr = target;
        // 5. Simple (n=1, i=1) 또는 default
        return new int[]{target, 0, formatLength};
    }


    private int signExtend(int value, int bitCount) {
        if ((value & (1 << (bitCount - 1))) != 0) {
            return value | (~((1 << bitCount) - 1));
        }
        return value;
    }

    @FunctionalInterface
    interface InstructionHandler {
        void execute(int pc);
    }
}