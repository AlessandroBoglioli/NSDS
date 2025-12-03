#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <mpi.h>

const int num_iter_per_proc = 10 * 1000 * 1000;

int main() {
  MPI_Init(NULL, NULL);
    
  int rank;
  int num_procs;
  int sum;
  
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  MPI_Comm_size(MPI_COMM_WORLD, &num_procs);

  srand(time(NULL) + rank);

  double x, y;
  int localSum = 0;

  for (int i = 0; i < num_iter_per_proc; i++) {
    x = (double)rand() / (double)RAND_MAX;
    y = (double)rand() / (double)RAND_MAX;

    if (sqrt(x*x + y*y) <= sqrt(1.0) && x*x + y*y <= 1.0) {
      localSum++;
    }
  }

  MPI_Reduce(&localSum, &sum, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
  
  if (rank == 0) {
    double pi = (4.0*sum) / (num_iter_per_proc*num_procs);
    printf("Pi = %f\n", pi);
  }
    
  MPI_Finalize();
  return 0;
}
