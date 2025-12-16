***

## 1. Introduction to IoT Software

This section defines the Internet of Things (IoT) and outlines the basic structure and constraints of IoT software.

### What is the IoT?
The key idea across definitions is that while the traditional Internet is about **data created by people**, the Internet of Things is about **data created by things**. It is a network of physical objects that are provided with unique identifiers and the ability to transfer data without requiring human interaction. The purpose is to sense, collect, transmit, analyze, and distribute data to gain knowledge.

### Anatomy of an IoT Device
A typical IoT device includes a CPU, Program Memory (Flash), Volatile Memory (RAM), Sensors, Actuators, a Radio for communication, and a Power Source.

### IoT Operating Systems (OSs)
Embedded IoT OSs differ significantly from mainstream operating systems.
* **Functionality:** They provide basic run-time support, including a Hardware Abstraction Layer (HAL), drivers, concurrency support, a networking stack, and developer tools.
* **Compilation:** Programs are **cross-compiled** and linked with the OS, meaning the OS acts more like a library. The final binary is then deployed onto the device.
* **Constraints:** The two main challenges are **Energy** (devices are battery-powered, and communication is the largest energy drain) and **Reliability** (wireless link quality is highly fluctuating).

***

## 2. Contiki-NG: Concurrency, Timers, and Simulation

Contiki-NG is a mature OS focused on RFC-compliant IPv6 networking and is known for its **event-driven kernel** and unique concurrency model.

### Protothreads
Protothreads is Contiki-NG's concurrency model designed to be highly memory-efficient:
* **Single Stack:** Protothreads maintain **sequential semantics** but achieve this with a **single stack** for all processes. This gives them a small memory footprint, unlike traditional multi-threading, where each thread requires its own stack.
* **Cooperative Scheduling:** Protothreads are scheduled **cooperatively** and process incoming **events** (like a timer expiring or a packet arriving). A process explicitly decides when to release the Microcontroller Unit (MCU).
* **Static Variables:** When a protothread yields, it saves its location and resumes later. Because of this, any local variables must be declared as `static` to retain their value across yields.

### Timers
Contiki-NG provides several timer types for different needs:
* **`timer`**: A low-level timer that requires the program to manually check if it has expired.
* **`etimer` & `stimer`**: Timers used to **generate events** that wake up protothreads. `etimer` is standard, and `stimer` is for longer periods.
* **`ctimer`**: Schedules a function to be executed as an **asynchronous callback** when the timer expires.
* **`rtimer`**: A high-resolution timer that can **preempt** the currently running protothread to execute a callback function ("Execute now" semantics).

### COOJA Simulator
COOJA is the Contiki-NG simulator, which allows you to define a simulation **scenario** including the number, type, and placement of nodes, as well as the wireless channel model.
* **COOJA Motes:** Run as C processes in the Java Virtual Machine (JVM). They are fast because local processing **takes no simulation time**, but time-dependent behaviors are not accurately represented.
* **Sky Motes (and other MSP430 platforms):** Use the **MSPSim emulator** for cycle-accurate emulation of every binary instruction. This is **time-accurate** but significantly **slows down** the simulation.
* **Channel Models:** Include basic models like the **Unit-disk Graph Models (UDGM)**, the simplified **Directed Graph Model** (which ignores interference), and the sophisticated **Multi-path Raytracer Model (MRM)** that accounts for physical obstacles.

***

## 3. IoT Networking and Protocols

### Low-power Communications
Since the energy cost to reach longer distances grows **quadratically**, IoT networks rely on **low-power multi-hop communications**.

| Protocol Stack Layer | IoT Protocols/Technologies | Key Considerations |
| :--- | :--- | :--- |
| **Link Layer** | 802.15.4, Bluetooth LE, LoRa, SigFox | A "melting pot" of various standards focused on energy reduction. |
| **IP Layer** | **IPv6** with **6LoWPAN** | 6LoWPAN (IPv6 over Low-power Wireless Personal Area Networks) is an adaptation layer used because standard IPv6 is too heavy for resource-constrained devices. |
| **Routing Layer** | **RPL** (RFC 6550), Thread | Focuses on **Multi-hop mesh networking**. RPL is an open specification for 802.15.4 networks. |
| **Transport Layer** | **UDP** (preferred), TCP | **UDP** is generally preferred because TCP is costly, especially in multi-hop scenarios, and its congestion control is not suited for unreliable wireless links. |
| **Application Layer** | **CoAP**, **MQTT** | **CoAP** is a lightweight, binary, REST-like protocol over UDP. **MQTT** is a lightweight, message-oriented, Publish/Subscribe protocol that uses TCP for QoS. |

### RPL Routing
**RPL (Routing Protocol for Low-Power and Lossy Networks)** creates a **tree-shaped topology** called a **Destination-Oriented Directed Acyclic Graph (DODAG)**.
* **DODAG Root:** The tree is rooted at the node with direct Internet access, known as the DODAG root.
* **Traffic:** It naturally supports **many-to-one** communication (from nodes up to the root). One-to-one and one-to-many communication is supported when nodes advertise themselves upwards using **DAO (Destination Advertisement Object)** packets.
* **Objective Functions:** RPL uses objective functions to select the **preferred parent** in the tree. Examples in Contiki-NG include:
    * **OF0:** A simple function that looks for a "good-enough" parent.
    * **MRHOF (Minimum Rank Hysteresis Objective Function):** More efficient, as it looks for the minimum rank and uses hysteresis to avoid unstable parent-flipping.

### MQTT in Contiki-NG
* **RPL Border Router:** This is a crucial "man-in-the-middle" component that **bridges the RPL network with the external Internet**. In Contiki-NG, it is configured to be the **root of the RPL tree**.
* **MQTT Support:** Contiki-NG's implementation supports MQTT v.3.1 (without QoS2) and allows devices to act as both a **publisher** and a **subscriber**. The API provides functions for registering, connecting to a broker, and publishing/subscribing to topics.

***

## 4. Contiki-NG Cheat Sheet

This sheet provides practical guidance for setting up the development environment:
* **Virtual Machine (VM):** A customized VM is recommended for working with Contiki-NG, as it's the most reliable way to use real IoT embedded hardware.
* **Source Code:** A private fork of Contiki-NG is used for the course, which is pulled with standard `git` commands.
* **Compilation:** To compile code for your host machine (native platform), use the command: `make TARGET=native`.
* **COOJA Setup:** The COOJA simulator is started with the command `ant run` from the `tools/cooja` directory.
* **RPL Border Router Setup:** The border router can run inside COOJA. To bridge the simulated network to the real IPv6 network, you must execute `make TARGET=cooja connect-router-cooja` from the command line after starting the simulation.
* **Mosquitto:** The VM comes with the Mosquitto MQTT broker pre-installed and running.