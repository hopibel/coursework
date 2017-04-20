#include "cdecl.h"

int PRE_CDECL asm_main( void ) asm ("asm_main") POST_CDECL;

int main()
{
  int ret_status;
  ret_status = asm_main();
  return ret_status;
}
