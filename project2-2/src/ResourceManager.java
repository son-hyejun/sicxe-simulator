import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다. 크게 네가지의 가상 자원 공간을 선언하고, 이를
 * 관리할 수 있는 함수들을 제공한다.
 *
 *
 * 1) 입출력을 위한 외부 장치 또는 device 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.
 * 3) 연산을 수행하는데 사용하는 레지스터 공간. 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들.
 *
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면, 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서
 * 차이가 있다.
 */
public class ResourceManager {
    /**
     * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다. 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의
     * 파일을 의미한다. deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
     * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을
     * 넣음으로서 이를 관리할 수 있다.
     *
     * 변형된 형태로 사용하는 것 역시 허용한다. 예를 들면 key값으로 String대신 Integer를 사용할 수 있다. 파일 입출력을 위해
     * 사용하는 stream 역시 자유로이 선택, 구현한다.
     *
     * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
     */
    HashMap<String, Object> deviceManager = new HashMap<String, Object>();
    byte[] memory = new byte[65536]; // String으로 수정해서 사용하여도 무방함.
    int[] register = new int[10];
    double register_F;

    SymbolTable symtabList;
    // 이외에도 필요한 변수 선언해서 사용할 것.
    
    // 디바이스 읽기에 사용되는 변수
    HashMap<String, Integer> offsets = new HashMap<>();

    // control section 정보
    // [이름, 시작 주소, 길이]
    public HashMap<Integer, String[]> csInfos;

    int memStartAddr;

    private static final HashMap<String, Integer> regMap = new HashMap<>();
    static {
        regMap.put("A", 0);
        regMap.put("X", 1);
        regMap.put("L", 2);
        regMap.put("B", 3);
        regMap.put("S", 4);
        regMap.put("T", 5);
        regMap.put("F", 6);
        regMap.put("PC", 8);
        regMap.put("SW", 9);
    }

    int progStartAddr;

    /**
     * 메모리, 레지스터등 가상 리소스들을 초기화한다.
     */
    public void initializeResource() {
        Arrays.fill(memory, (byte) 0);
        Arrays.fill(register, 0);
        register_F = 0.0;
        progStartAddr = 0;

        csInfos = new HashMap<>();
        memStartAddr = 0x0;
    }

    /**
     * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할. 프로그램을 종료하거나 연결을 끊을 때
     * 호출한다.
     */
    public void closeDevice() {
        for (String key : deviceManager.keySet()) {
            Object obj = deviceManager.get(key);

            try {
                if (obj instanceof Closeable) {
                    ((Closeable) obj).close();
                }
                if (obj instanceof Flushable) {
                    ((Flushable) obj).flush();
                }
            } catch (Exception e) {
                System.out.println("Error closing device " + key);
            }
        }
        deviceManager.clear();
    }

    /**
     * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수. 입출력 stream을 열고 deviceManager를
     * 통해 관리시킨다.
     *
     * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
     */
    public void testDevice(String devName) {
        if (!deviceManager.containsKey(devName)) {
            try {
                File file = new File(devName);
                if (!file.exists()) {
                    file.createNewFile();
                }

                // reader
                BufferedReader reader = new BufferedReader(new FileReader(file));
                deviceManager.put(devName + "_reader", reader);

                // writer
                BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
                deviceManager.put(devName + "_writer", writer);

                setRegister("SW", (byte) 1);
            } catch (IOException e) {
                setRegister("SW", (byte) 0);
            }
        } else {
            setRegister("SW", (byte) 1);
        }
    }

    /**
     * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
     *
     * @param devName 디바이스의 이름
     * @param num     가져오는 글자의 개수
     * @return 가져온 데이터
     */
    public byte[] readDevice(String devName, int num) {
        byte[] buf = new byte[num];
        BufferedReader reader = (BufferedReader) deviceManager.get(devName + "_reader");

        if (reader == null) {
            throw new IllegalStateException("Device " + devName + " is not open");
        }

        try {
            if (offsets.containsKey(devName)) {
                offsets.put(devName, offsets.get(devName) + 1);
            } else {
                offsets.put(devName, 0);
            }

            int offset = offsets.get(devName);

            for (int i = 0; i < offset; i++) reader.read();

            int readCount = 0;

            while (readCount < num) {
                int c = reader.read();
                if (c == -1) break;
                buf[readCount++] = (byte) c;
            }

            if (readCount < num) {
                setRegister("A", 0);
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read device: " + devName, e);
        }

        return buf;
    }

    /**
     * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
     *
     * @param devName 디바이스의 이름
     * @param data    보내는 데이터
     * @param num     보내는 글자의 개수
     */
    public void writeDevice(String devName, byte[] data, int num) {
        BufferedWriter writer = (BufferedWriter) deviceManager.get(devName + "_writer");

        if (writer == null) {
            throw new IllegalStateException("Device " + devName + " is not open");
        }

        try {
            for (int i = 0; i < num; i++) {
                writer.write((char) data[i]);
            }
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write device: " + devName, e);
        }
    }

    /**
     * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
     *
     * @param location 메모리 접근 위치 인덱스
     * @param num      데이터 개수
     * @return 가져오는 데이터
     */
    public byte[] getMemory(int location, int num) {
        byte[] result = new byte[num];
        for (int i = 0; i < num; i++) {
            result[i] = readByte(location + i);
        }
        return result;
    }

    /**
     * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다.
     *
     * @param locate 접근 위치 인덱스
     * @param data   저장하려는 데이터
     * @param num    저장하는 데이터의 개수
     */
    public void setMemory(int locate, byte[] data, int num) {
        for (int i = 0; i < num; i++) {
            writeByte(locate + i, data[i]);
        }
    }

    /**
     * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
     * @param regNum 레지스터 분류번호
     * @return 레지스터가 소지한 값
     */
    public int getRegister(int regNum) {
        return register[regNum];
    }

    public int getRegister(String name) {
        Integer idx = regMap.get(name);
        if (idx == null) throw new IllegalArgumentException("No such register " + name);
        return getRegister(idx);
    }

    /**
     * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
     *
     * @param regNum 레지스터의 분류번호
     * @param value  레지스터에 집어넣는 값
     */
    public void setRegister(int regNum, int value) {
        register[regNum] = value;
    }

    public void setRegister(String name, int value) {
        Integer idx = regMap.get(name);
        if (idx == null) throw new IllegalArgumentException("No such register " + name);
        setRegister(idx, value);
    }

    /**
     * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 byte[]형태로 변경한다.
     *
     * @param data
     * @return
     */
    public byte[] intToBytes(int data) {
        byte[] result = new byte[3];
        result[0] = (byte) ((data >> 16) & 0xFF);
        result[1] = (byte) ((data >> 8) & 0xFF);
        result[2] = (byte) (data & 0xFF);

        return result;
    }

    /**
     * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. byte[]값을 int형태로 변경한다.
     *
     * @param data
     * @return
     */
    public int bytesToInt(byte[] data) {
        if (data.length != 3) throw new IllegalArgumentException("Only 3 byte input supported");
        return ((data[0] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[2] & 0xFF);
    }

    public byte readByte(int address) {
        return memory[address & 0xFFFF];
    }

    public void writeByte(int address, byte data) {
        memory[address & 0xFFFF] = data;
    }

    public int readWord(int address) {
        return ((memory[address & 0xFFFF] & 0xFF) << 16)
                | ((memory[(address + 1) & 0xFFFF] & 0xFF) << 8)
                | (memory[(address + 2) & 0xFFFF] & 0xFF);
    }

    public void writeWord(int address, int value) {
        memory[address & 0xFFFF] = (byte) ((value >> 16) & 0xFF);
        memory[(address + 1) & 0xFFFF] = (byte) ((value >> 8) & 0xFF);
        memory[(address + 2) & 0xFFFF] = (byte) (value & 0xFF);
    }

    public void setProgramStart(int progStartAddr) {
        this.progStartAddr = progStartAddr;
    }
}