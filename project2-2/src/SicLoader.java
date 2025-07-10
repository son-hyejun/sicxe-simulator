import java.io.*;
import java.util.ArrayList;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다.
 *
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다. - program code를 메모리에 적재시키기 - 주어진 공간만큼 메모리에 빈
 * 공간 할당하기 - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
    ResourceManager rMgr;
    ArrayList<SymbolTable> symbolTables;

    SymbolTable currSymbolTable;
    int csAddr; // 현재 Control Section 시작 주소
    int progLength;


    public SicLoader(ResourceManager resourceManager) {
        // 필요하다면 초기화
        setResourceManager(resourceManager);
        rMgr.initializeResource();
        symbolTables = new ArrayList<>();
        csAddr = 0;
    }

    /**
     * Loader와 프로그램을 적재할 메모리를 연결시킨다.
     *
//     * @param rMgr
     */
    public void setResourceManager(ResourceManager resourceManager) {
        this.rMgr = resourceManager;
    }

    /**
     * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록
     * 한다. load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
     *
     * @param objectCode 읽어들인 파일
     */
    public void load(File objectCode) {
        try (BufferedReader reader = new BufferedReader(new FileReader(objectCode))) {
            String line;
            reader.mark(65536);
            csAddr = rMgr.memStartAddr;

            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;

                char recordType = line.charAt(0);
                line = line.substring(1);

                switch (recordType) {
                    case 'H' -> handleHeader(line);
                    case 'D' -> handleDefine(line);
                    case 'T' -> handleText(line);
                    case 'E' -> handleEnd(line);
                }
            }

            reader.reset();
            csAddr = rMgr.memStartAddr;
            progLength = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                char recordType = line.charAt(0);
                line = line.substring(1);

                if (recordType == 'H') handleHeader(line);
                if (recordType == 'M') handleModify(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    };

    private void handleHeader(String line) {
        String[] tokens = line.trim().split("\\s+");
        String progName = tokens[0];
        String startAndLength = tokens[1];

        csAddr += progLength;
        progLength = Integer.parseInt(startAndLength.substring(6, 12), 16);


        currSymbolTable = new SymbolTable();
        currSymbolTable.putSymbol(progName, csAddr);

        String[] csInfo = new String[3];
        csInfo[0] = progName;
        csInfo[1] = String.format("%06X", csAddr);
        csInfo[2] = String.format("%04X", progLength);

        rMgr.csInfos.put(csAddr, csInfo);
    }

    private void handleDefine(String line) {
        for (int i = 0; i < line.length(); i += 12) {
            if (i + 12 > line.length()) break;

            String symbol = line.substring(i, i + 6).trim();
            int addr = Integer.parseInt(line.substring(i + 6, i + 12), 16) + csAddr;
            currSymbolTable.putSymbol(symbol, addr);
        }
    }

    private void handleText(String line) {
        int start = csAddr;
        start += Integer.parseInt(line.substring(0, 6), 16);

        int length = Integer.parseInt(line.substring(6, 8), 16);
        String objectCodes = line.substring(8);

        for (int i = 0; i < length; i++) {
            int val = Integer.parseInt(objectCodes.substring(i * 2, i * 2 + 2), 16);
            rMgr.writeByte(start + i, (byte) val);
        }
    }

    private void handleModify(String line) {
        int addr = Integer.parseInt(line.substring(0, 6), 16) + csAddr;
        int halfBytes = Integer.parseInt(line.substring(6, 8), 16);
        int bytes = (halfBytes + 1) / 2;

        char sign = line.charAt(8);
        String symbol = line.substring(9);

        int symAddr = -1;
        for (SymbolTable s : symbolTables) {
            symAddr = s.search(symbol);
            if (symAddr != -1) break;
        }
        if (symAddr == -1) {
            throw new RuntimeException("Symbol " + symbol + " not found");
        }

        byte[] originalBytes = rMgr.getMemory(addr, bytes);
        int originalVal = rMgr.bytesToInt(originalBytes);

        int newVal = sign == '+' ? originalVal + symAddr : originalVal - symAddr;
        byte[] newBytes = rMgr.intToBytes(newVal);

        rMgr.setMemory(addr, newBytes, bytes);
    }

    private void handleEnd(String line) {
        if (line.isEmpty()) rMgr.setProgramStart(csAddr);
        else {
            int startAddr = Integer.parseInt(line.substring(1).trim(), 16) + rMgr.memStartAddr;
            rMgr.setProgramStart(startAddr);
        }

        symbolTables.add(currSymbolTable);
    }
}
