
-----

# Essential MPI Methods for Exam Prep

The Message Passing Interface (MPI) is a standard for high-performance distributed computing (HPC). It standardizes an API for communicating between processes in a parallel program.

## 1\. Environment Management & Initialization

These functions are used to set up and tear down the MPI environment, and to query fundamental information about the running processes.

| Function | Signature | Description |
| :--- | :--- | :--- |
| `MPI_Init` | `int MPI_Init(int *argc, char ***argv)` | **Initializes** the MPI execution environment. Must be called once before any other MPI routine. |
| `MPI_Finalize` | `int MPI_Finalize(void)` | **Terminates** the MPI execution environment. Must be called when all message-passing is complete. |
| `MPI_Comm_size` | `int MPI_Comm_size(MPI_Comm comm, int *size)` | Returns the **total number of MPI processes** (`size`) in the specified communicator (`comm`). |
| `MPI_Comm_rank` | `int MPI_Comm_rank(MPI_Comm comm, int *rank)` | Returns the **rank** (integer ID) of the calling process (`rank`) within the specified communicator (`comm`). Ranks are contiguous, starting at zero. |
| `MPI_Get_processor_name` | `int MPI_Get_processor_name(char *name, int *len)` | Returns the **processor name** where the calling process is running. |

-----

## 2\. Point-to-Point Communication

These primitives facilitate message exchange **from a single sender to a single recipient**.

### Sending and Receiving Primitives

| Primitive | Blocking/Non-blocking | Synchronous/Asynchronous | Description |
| :--- | :--- | :--- | :--- |
| `MPI_Send` | **Blocking** | Asynchronous (might buffer) | Sends a message. Only returns when it is safe to modify the application buffer for reuse. |
| `MPI_Recv` | **Blocking** | N/A | Receives a message. Only returns when the data has arrived and is ready for use. |
| `MPI_Isend` | **Non-blocking** | Asynchronous | Initiates an asynchronous send operation. Does not wait for communication to happen. |
| `MPI_Irecv` | **Non-blocking** | N/A | Initiates an asynchronous receive operation. Does not wait for communication to happen. |
| `MPI_Ssend` | **Blocking** | Synchronous | Sends a message with a guarantee of synchronization (waits for confirmation from the receiver). |
| `MPI_Sendrecv` | **Blocking** | N/A | Sends a message and posts a (blocking) receive before blocking. Useful for safe exchange operations to avoid deadlocks. |
| `MPI_Wait` | **Blocking** | N/A | Waits for a specific non-blocking operation (e.g., initiated by `MPI_Isend` or `MPI_Irecv`) to complete. |
| `MPI_Waitall` | **Blocking** | N/A | Waits for all specified non-blocking operations to complete (e.g., used in deadlock avoidance with `MPI_Isend`/`MPI_Irecv`). |
| `MPI_Probe` | **Blocking** | N/A | Checks for the availability of a message without actually receiving it. Fills an `MPI_Status` structure with sender, tag, and message size. |

### Function Signatures (General)

* `MPI_Send` / `MPI_Ssend` / `MPI_Isend` (Simplified):
  ```c
  int MPI_Send(void* data, int count, MPI_Datatype datatype, 
               int destination, int tag, MPI_Comm communicator); 
  ```
  * **`destination`**: Rank of the recipient process.
  * **`tag`**: Integer used to differentiate message types.
* `MPI_Recv` / `MPI_Irecv` (Simplified):
  ```c
  int MPI_Recv(void* data, int count, MPI_Datatype datatype, 
               int source, int tag, MPI_Comm communicator, 
               MPI_Status* status);
  ```
  * **`source`**: Rank of the sender process (can be set to `MPI_ANY_SOURCE`).
  * **`status`**: Used to return information about the completed receive.

### Key MPI Datatypes (Examples)

MPI defines its own data types for portability.

| MPI Datatype | C Equivalent |
| :--- | :--- |
| `MPI_INT` | `int` |
| `MPI_FLOAT` | `float` |
| `MPI_DOUBLE` | `double` |
| `MPI_CHAR` / `MPI_BYTE` | `char` |

-----

## 3\. Collective Communication

These procedures involve **a group of processes** collaborating on a common goal, typically within the scope of a communicator like `MPI_COMM_WORLD`.

### Synchronization

| Function | Signature | Description |
| :--- | :--- | :--- |
| `MPI_Barrier` | `int MPI_Barrier(MPI_Comm communicator)` | **Synchronizes** all processes in the communicator. No process is allowed to continue past the call until *all* processes have reached the barrier. |

### Data Movement and Reduction

| Function | Signature (Simplified) | Description |
| :--- | :--- | :--- |
| `MPI_Bcast` | `int MPI_Bcast(void* data, int count, MPI_Datatype datatype, int root, MPI_Comm communicator)` | **Broadcasts** data from a single **root** process to all other processes in the communicator. All processes call this function. |
| `MPI_Reduce` | `int MPI_Reduce(void* send_data, void* recv_data, int count, MPI_Datatype datatype, MPI_Op op, int root, MPI_Comm communicator)` | **Combines** the data (`send_data`) from all processes using an operation (`op`), storing the final result in the `recv_data` buffer of the **root** process. |
| `MPI_Allreduce` | (Similar to Reduce, no root) | Performs a reduction operation and **stores the result at every process** in the communicator. |
| `MPI_Scatter` | `int MPI_Scatter(void* send_data, int send_count, ..., void* recv_data, int recv_count, ..., int root, MPI_Comm communicator)` | **Distributes** an array of data, sending a segment (`send_count` elements) from the **root** process to *each* receiving process. |
| `MPI_Gather` | `int MPI_Gather(void* send_data, int send_count, ..., void* recv_data, int recv_count, ..., int root, MPI_Comm communicator)` | **Collects** data, receiving a segment (`send_count` elements) from *each* process and assembling the full result in the `recv_data` buffer of the **root** process. |

### Reduce Operators (`MPI_Op`)

These are pre-defined operations used in `MPI_Reduce` and similar functions.

| Operator | Operation |
| :--- | :--- |
| `MPI_SUM` | Sums the elements. |
| `MPI_PROD` | Multiplies the elements. |
| `MPI_MAX` | Returns the maximum element. |
| `MPI_MIN` | Returns the minimum element. |
| `MPI_MAXLOC` | Returns the maximum value and the rank of the process that owns it. |
| `MPI_MINLOC` | Returns the minimum value and the rank of the process that owns it. |
