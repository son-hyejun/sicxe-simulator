<h1 align="center">
  SIC/XE Simulator 🖥️
</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/Swing-007396?style=flat&logo=java&logoColor=white"/>
  <img src="https://img.shields.io/badge/Simulator-3DC5A0?style=flat"/>
</p>

<p align="center">
  A Java-based simulator that loads and executes SIC/XE object programs with a visual touch.
</p>

---

## 🚀 Overview

This project is a **SIC/XE simulator** that visually loads and executes object programs (.obj files) in a Java GUI environment.  
It was developed as part of the **System Programming course assignment**, aiming to deeply understand **loader mechanisms**, **instruction decoding**, and the inner workings of the SIC/XE architecture.

---

## ✨ Features

- ✅ Load and parse `.obj` files (Header, Define, Text, Modify, End records)
- ✅ Decode and execute SIC/XE instructions (Format 2/3/4)
- ✅ Support multiple addressing modes (Immediate, Indirect, Simple)
- ✅ GUI-based step-by-step or full execution
- ✅ Real-time visualization of registers, memory, and execution logs
- ✅ Device I/O simulation via virtual file devices
- ✅ Memory highlighting for current instructions

---

## 💡 Architecture
```yml
src/
├─ VisualSimulator.java # Controls loading & execution flow
├─ SimulatorUI.java # Swing-based GUI (the pretty face)
├─ ResourceManager.java # Memory, registers, and device manager
├─ SicLoader.java # Parses and loads object programs
├─ SymbolTable.java # Manages symbols and addresses
├─ SicSimulator.java # Orchestrates instruction execution
└─ InstLuncher.java # Decodes opcodes & runs handlers
```
---

## 🛠️ How to Use

1. Prepare an `.obj` object file
2. Run the program
3. Use the **file selection button** to load your object file
4. Click **Step** to execute one instruction at a time, or **All** to run everything
5. Watch registers, memory, devices, and logs update in real time

---

## ⚙️ Technical Highlights

- **Java Swing GUI** with dynamic updates
- **64KB virtual memory** & comprehensive register set (A, X, L, B, S, T, F, PC, SW)
- Loader with **symbol resolution** and relocation support
- Flexible instruction decoding with opcode handlers
- Virtual device file I/O simulation
- Execution logs and memory highlighting for clarity

---

## 💬 Conclusion

Through this project, I explored how programs are loaded and executed at a low level — far beyond "just running it."  
I got to implement what’s usually under the hood: loaders, relocations, instruction decoding, and memory/register management.

> _"Not just a simulator, but a peek into the soul of your object code."_ 👀

---

<p align="center">
  👩‍💻 Check out my [GitHub profile](https://github.com/son-hyejun) for more!
</p>


