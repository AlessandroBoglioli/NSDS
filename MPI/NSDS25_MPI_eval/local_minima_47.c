#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>
#include <string.h>

// Group number: 47
// Group members:
// - Boglioli Alessandro
// - Colombi Riccardo
// - Limoni Pietro

const int N = 256;

// Allocates and initializes matrix
int* generate_matrix() {
  int* A = (int*) malloc(N * N * sizeof(int));
  for (int i = 0; i < N * N; i++) {
    A[i] = rand() % 100;
  }
  return A;
}

// Returns the value at the given row and column
int val(int *A, int r, int c) {
  return A[r * N + c];
}

int main(int argc, char** argv) {
  MPI_Init(&argc, &argv);

  int rank, size;
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &size);

  if (N % size != 0) {
    if (rank == 0) printf("N must be a multiple of the number of processes.\n");
    MPI_Finalize();
    return 0;
  }

  // ------------------------------------------------------------
  // Generate matrix on rank 0
  // ------------------------------------------------------------

  int* fullA = NULL;
  
  if (rank == 0) {
      fullA = generate_matrix();
  }

  // ------------------------------------------------------------
  // Distribute to all processes
  // ------------------------------------------------------------

  int rows_per_procs = N / size;
  int element_per_procs = rows_per_procs * N;

  int* local =  (int*) malloc(element_per_procs * sizeof(int));
  int* precRow = (int*) malloc(N * sizeof(int));
  int* succRow = (int*) malloc(N * sizeof(int));

  MPI_Scatter(fullA, element_per_procs, MPI_INT, 
              local, element_per_procs, MPI_INT, 
              0, MPI_COMM_WORLD);


  // ------------------------------------------------------------
  // Free global matrix
  // ------------------------------------------------------------

  // if (rank == 0)
  //   for (int i = 0; i < N; i++) {
  //     for (int j = 0; j < N; j++) {
  //       printf("%d ", val(fullA, i, j));
  //     }
  //     printf("\n");
  //   }
  
  free(fullA);
  
  // ------------------------------------------------------------
  // Exchange futher information if needed
  // ------------------------------------------------------------

  if (rank > 0) {
    MPI_Sendrecv(local, N, MPI_INT, rank - 1, 0,
                 precRow, N, MPI_INT, rank - 1, 0,
                 MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  }
  if (rank < size - 1) {
    MPI_Sendrecv(&local[element_per_procs - N], N, MPI_INT, rank + 1, 0,
                 succRow, N, MPI_INT, rank + 1, 0,
                 MPI_COMM_WORLD, MPI_STATUS_IGNORE);
  }
  
  // ------------------------------------------------------------
  // Compute local minima (excluding GLOBAL borders)
  // ------------------------------------------------------------

  int* count = (int*) malloc(rows_per_procs * sizeof(int));
  memset((void*) count, 0, rows_per_procs * sizeof(int));

  for (int i = 0; i < rows_per_procs; i++) {
    if (rank == 0 && i == 0) continue;
    if (rank == size - 1 && i == rows_per_procs - 1) continue;

    for (int j = 1; j < N - 1; j++) {
      
      if (val(local, i, j) > val(local, i, j - 1) || val(local, i, j) > val(local, i, j + 1))
        continue;

      if (i == 0) {
        if (val(local, i, j) < precRow[j] && val(local, i, j) < val(local, i + 1, j))
          count[i]++;
      }
      else if (i == rows_per_procs - 1) {
        if (val(local, i, j) < succRow[j] && val(local, i, j) < val(local, i - 1, j))
          count[i]++;
      }
      else if (val(local, i, j) < val(local, i - 1, j) 
              && val(local, i, j) < val(local, i + 1, j)) {
        count[i]++;
      }
    }
  }
  
  // ------------------------------------------------------------
  // Send results to rank 0 and print results on rank 0
  // ------------------------------------------------------------

  int* globalCount = (int*) malloc(N * sizeof(int));

  MPI_Gather(count, rows_per_procs, MPI_INT, 
             globalCount, rows_per_procs, MPI_INT, 
             0, MPI_COMM_WORLD);

  if (rank == 0) {
    for (int i = 0; i < N; i++) {
      printf("Row %d: local minima: %d\n", i, globalCount[i]);
    }
  }
  
  // ------------------------------------------------------------
  // Free allocated memory
  // ------------------------------------------------------------

  free(local);
  free(precRow);
  free(succRow);
  free(count);
  free(globalCount);

  MPI_Finalize();
  return 0;
}
