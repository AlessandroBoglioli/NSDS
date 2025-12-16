***

## 1. Protothreads: Process Definition and Control

Protothreads in Contiki-NG are defined using a set of special C preprocessor macros to create a cooperative, event-driven process.

| Concept | Macro/Function | Purpose & Usage | Source |
| :--- | :--- | :--- | :--- |
| **Process Definition** | `PROCESS_THREAD(name, ev, data)` | Defines the body of the protothread function, where `ev` is the event type and `data` is the data payload associated with the event. | |
| **Start/End** | `PROCESS_BEGIN();` `PROCESS_END();` | Must wrap the entire body of the process thread function. | |
| **Wait for Any Event** | `PROCESS_WAIT_EVENT();` | The process yields (pauses) and waits for the next event to be posted to it. | |
| **Wait for Specific Event** | `PROCESS_WAIT_EVENT_UNTIL(condition);` | The process yields and waits until a condition (like a timer expiring or a flag being set) is true **after** an event has been received. | |
| **Post an Event** | `process_post(&target_process, event_id, &data);` | Used to send an event from one protothread to another, optionally passing a data pointer. | |
| **Memory Constraint** | `static` keyword | Any local variables that must retain their value across a `PROCESS_WAIT_...` or `PROCESS_YIELD` must be declared `static`. | |

## 2. Contiki-NG Timers: API and Semantics

The timers differ based on whether they generate an **event** (cooperative scheduling) or execute a **callback** (asynchronous execution).

| Timer Type | Key Function / Macro | Code Example and Explanation | Source |
| :--- | :--- | :--- | :--- |
| **`etimer`** (Event) | `etimer_set()` | **Set:** `etimer_set(&periodic_timer, CLOCK_SECOND * 5);` This sets the timer to expire after 5 seconds (5 system ticks). | |
| | `etimer_expired()` | **Wait:** `PROCESS_WAIT_EVENT_UNTIL(etimer_expired(&periodic_timer));` Used inside a protothread to wait until the timer expires. | |
| **`ctimer`** (Callback) | `ctimer_set()` | **Schedule:** `ctimer_set(&my_timer, CLOCK_SECOND, callback_func, data_ptr);` Schedules `callback_func` to run after 1 second. The `data_ptr` can pass information to the callback. | |
| | Callback Signature | `static void callback_func(void *data) { ... }` This function runs asynchronously when the timer expires, **after** the currently running protothread completes. | |
| **`rtimer`** (Real-Time) | `rtimer_set()` | **Schedule:** `rtimer_set(&rtimer, RTIMER_NOW() + RTIMER_SECOND, 0, rtimer_callback, data_ptr);` Schedules the callback at an absolute time (`RTIMER_NOW()` is the current time) using the hardware's maximum clock resolution, providing **preemption**. | |
| | Callback Signature | `static void rtimer_callback(struct rtimer *t, void *data) { ... }` This function **preempts** any running protothread to execute immediately. | |

***

## 3. RPL Routing and UDP Networking (`simple-udp` API)

The `simple-udp` API simplifies IPv6/UDP communication over RPL.

| Component | Function/Structure | Purpose & Usage | Source |
| :--- | :--- | :--- | :--- |
| **Connection Struct** | `struct simple_udp_connection` | A structure used to hold the state of a UDP connection, including local/remote ports and the receive callback. | |
| **Initialize Layer** | `void simple_udp_init(void);` | Initializes the underlying UDP stack. | |
| **Register** | `simple_udp_register(c, lport, raddr, rport, cb);` | Sets up a UDP connection. Use `NULL` for the `raddr` (remote IP address) if you plan to send to multiple destinations (like the DODAG root). | |
| **Send Data** | `simple_udp_sendto(c, data, datalen, to_ipaddr);` | Sends a UDP packet (`data` of size `datalen`) to a specific IPv6 address (`to_ipaddr`). | |
| **Check Route** | `NETSTACK_ROUTING.node_is_reachable()` | Checks if the node has successfully joined the RPL network and established a route (path) up to the DODAG root. | |
| **Get Root IP** | `NETSTACK_ROUTING.get_root_ipaddr(&dest_ipaddr);` | Populates `dest_ipaddr` with the IPv6 address of the DODAG root node, allowing the client to send data "up the tree". | |

## 4. MQTT API

Contiki-NG includes an MQTT client implementation compliant with v.3.1 (without QoS2).

| Function | Purpose & Usage | Source |
| :--- | :--- | :--- |
| **`mqtt_register()`** | `mqtt_register(conn, app_process, client_id, event_callback, max_segment_size);` Registers the connection structure, associates it with the main process, and sets the callback function to handle MQTT events (like connection success or incoming messages). | |
| **`mqtt_connect()`** | `mqtt_connect(conn, host, port, keep_alive);` Attempts to establish a connection to the MQTT broker, specified by its hostname or IP (`host`) and port. | |
| **`mqtt_subscribe()`** | `mqtt_subscribe(conn, mid, topic, qos_level);` Subscribes the device to a message topic. | |
| **`mqtt_publish()`** | `mqtt_publish(conn, mid, topic, payload, payload_size, qos_level, retain);` Publishes a message (`payload`) to a specific topic. | |