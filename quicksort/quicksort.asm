; CS131 Final project - Quicksort with various optimizations
; Hope Ibelgaufts

segment .data
	; ANSI color codes for output. should work in recent PowerShell versions
	ANSI_COLOR_REVERSE db `\x1b[7m`,0
	ANSI_COLOR_PIVOT db `\x1b[1;4m`,0
	ANSI_COLOR_SWAP db `\x1b[93;41m`,0
	ANSI_COLOR_MID db `\x1b[97;44m`,0
	ANSI_COLOR_RESET db `\x1b[0m`,0
	format_reset db "%.2f",`\x1b[0m`,"%s",0

	n db 0						;	int n = 0;

	prompt db "Array size (max 100): ",0
	scan_d db "%d",0
	prompt_float db "Enter a number (%d/%d): ",0
	scan_f db "%f",0

	format_float db "%.2f%s",0
	format_nl db 10,0
	format_tab db 9,0

segment .bss
	array resd 100				;	float array[100];
	size resd 1					;	int size;

segment .text
	global asm_main
	extern printf, scanf

asm_main:						;	int main (void)
	enter 0, 0					;	{
	pusha						;		int i;

do_input:						;		do {
	push prompt					;			printf("Array size (max 100): ");
	call printf
	add esp, 4

	push size					;			scanf("%d", &size);
	push scan_d
	call scanf
	add esp, 8

	cmp dword [size], 100		;		} while (size > SIZE || size < 1);
	jg do_input
	cmp dword [size], 1
	jl do_input

	mov ebx, 0
for_read:						;		for (i = 0; i < size; ++i) {
	cmp ebx, [size]
	jge .end

	push dword [size]			;			printf("Enter a number (%d/%d): ", i + 1, size);
	mov eax, ebx
	inc eax
	push eax
	push prompt_float
	call printf
	add esp, 12

	lea eax, [array + ebx*4]	;			scanf("%f", &array[i]);
	push eax
	push scan_f
	call scanf
	add esp, 8

	inc ebx
	jmp for_read
.end:							;		}

	mov ebx, 0
for_initial:					;		for (i = 0; i < size; ++i) {
	cmp ebx, [size]
	jge .end

	mov eax, [size]
	dec eax
	cmp ebx, eax				;			printf("%.2f%s", array[i], i == size - 1 ? "\n" : "\t");
	je .last
	push format_tab
	jmp .last.end
.last:
	push format_nl
.last.end:

	sub esp, 8					; allocate stack space for double
	fld dword [array + ebx*4]
	fstp qword [esp]

	push format_float
	call printf
	add esp, 16

	inc ebx
	jmp for_initial
.end:							;		}

	mov eax, [size]				;		quick_sort(array, 0, size - 1);
	dec eax
	push eax
	push dword 0
	push array
	call quick_sort
	add esp, 12

	popa						;		return 0;
	mov eax, 0					;	}
	leave
	ret

; subprogram: quick_sort
; Parameters:
;	hi at [ebp + 16]
;	lo at [ebp + 12]
;	a at [ebp + 8]
segment .text

%define hi ebp + 16
%define lo ebp + 12
%define a ebp + 8

%define i ebp - 4
%define j ebp - 8
%define k ebp - 12
%define n ebp - 16
%define swap ebp - 20
%define swap2 ebp - 24
%define swap3 ebp - 28
%define m ebp - 32
%define p ebp - 36
%define t ebp - 42

quick_sort:						;	void quick_sort(float *a, int lo, int hi)
								;	{
	push ebp
	mov ebp, esp
	sub esp, 42					;		int i, j, k, n, swap, swap2, swap3, m; float p, t;
	push ebx

	mov dword [swap], 0			;		swap = swap2 = swap3 = 0;
	mov dword [swap2], 0
	mov dword [swap3], 0

	mov eax, [hi]				;		int m = lo + (hi-lo)/2;
	sub eax, [lo]
	shr eax, 1
	add eax, [lo]
	mov [m], eax

	mov eax, [lo]				;		if (lo >= hi) {
	cmp eax, [hi]				;			return;
	jge end_quick_sort			;		}

;	/*
;	 * pivot selection
;	 * optimization: median of three partition selection. makes worst case O(n^2) less likely
;	 */

	mov eax, [a]
pivot_select:
.if:							;		if (a[lo] < a[m]) {
	mov ecx, [lo]

	fld dword [eax + ecx*4]
	mov edx, [m]
	fcomp dword [eax + edx*4]
	push eax
	fstsw ax
	sahf
	pop eax
	jnb .else

.if.if:							;			if (a[hi] < a[lo]) {
	mov ecx, [hi]
	fld dword [eax + ecx*4]
	mov edx, [lo]
	fcomp dword [eax + edx*4]
	push eax
	fstsw ax
	sahf
	pop eax
	jnb .if.end

	mov esi, [eax + edx*4]		;				t = a[lo];
	mov ebx, [eax + ecx*4]
	mov [eax + edx*4], ebx		;				a[lo] = a[hi];
	mov [eax + ecx*4], esi		;				a[hi] = t;
	mov dword [swap], 1			;				swap = swap3 = 1;
	mov dword [swap3], 1

.if.end:						;			}
	jmp .end

.else:							;		} else {
.else.if:						;			if (a[m] < a[hi]) {
	mov ecx, [m]
	fld dword [eax + ecx*4]
	mov edx, [hi]
	fcomp dword [eax + edx*4]
	push eax
	fstsw ax
	sahf
	pop eax
	jnb .else.else

	mov ecx, [lo]
	mov esi, [eax + ecx*4]		;				t = a[lo];
	mov edi, [m]
	mov ebx, [eax + edi*4]
	mov [eax + ecx*4], ebx		;				a[lo] = a[m];
	mov [eax + edi*4], esi		;				a[m] = t;
	mov dword [swap2], 1		;				swap2 = 1;

	jmp .else.end

.else.else:						;			} else {
	mov ecx, [lo]
	mov esi, [eax + ecx*4]		;				t = a[lo];
	mov edx, [hi]
	mov ebx, [eax + edx*4]
	mov [eax + ecx*4], ebx		;				a[lo] = a[hi];
	mov [eax + edx*4], esi		;				a[hi] = t;
	mov dword [swap3], 1		;				swap3 = 1;

.else.end:						;			}
	mov dword [swap], 1			;			swap = 1;
.end:							;		}

.second.if:						;		if (a[hi] < a[m]) {
	mov ecx, [hi]
	fld dword [eax + ecx*4]
	mov edx, [m]
	fcomp dword [eax + edx*4]
	push eax
	fstsw ax
	sahf
	pop eax
	jnb .second.end

	mov esi, [eax + edx*4]		;			t = a[m];
	mov ebx, [eax + ecx*4]
	mov [eax + edx*4], ebx		;			a[m] = a[hi];
	mov [eax + ecx*4], esi		;			a[hi] = t;
	mov dword [swap2], 1		;			swap2 = swap3 = 1;
	mov dword [swap3], 1
.second.end:					;		}

	mov ecx, [m]
	mov ebx, [eax + ecx*4]
	mov [p], ebx				;		p = a[m];

;	/* print state */
	mov ecx, 0
pivot_state:
.for:							;		for (k = 0; k < size; ++k) {
	cmp ecx, [size]
	jnl	.end

	cmp ecx, [m]				;			if (k == m) {
	jne .not_pivot

	push ecx
	push ANSI_COLOR_PIVOT
	call printf					;				printf(ANSI_COLOR_PIVOT);
	add esp, 4
	pop ecx
.not_pivot:						;			}

.for.if:						;			if ((swap && k == lo) || (swap2 && k == m) || (swap3 && k == hi)) {
	cmp dword [swap], 0
	je .for.if.swap2
	cmp ecx, [lo]
	jne .for.if.swap2
	jmp .for.if.end
.for.if.swap2:
	cmp dword [swap2], 0
	je .for.if.swap3
	cmp ecx, [m]
	jne .for.if.swap3
	jmp .for.if.end
.for.if.swap3:
	cmp dword [swap3], 0
	je .for.else
	cmp ecx, [hi]
	jne .for.else
.for.if.end:

	push ecx
	push ANSI_COLOR_SWAP
	call printf					;				printf(ANSI_COLOR_SWAP);
	add esp, 4
	pop ecx

	jmp .for.end
.for.else:						;			} else if (k >= lo && k <= hi) {
	cmp ecx, [lo]
	jl .for.end
	cmp ecx, [hi]
	jg .for.end

	push ecx
	push ANSI_COLOR_MID
	call printf					;				printf(ANSI_COLOR_MID);
	add esp, 4
	pop ecx

.for.end:							;			}

	push ecx
	mov ebx, [size]
	dec ebx
	cmp ecx, ebx
	je .last
	push format_tab
	jmp .last.end
.last:
	push format_nl
.last.end:

; convert float to double and store on stack
	sub esp, 8
	mov eax, [a]
	fld dword [eax + ecx*4]
	fstp qword [esp]

	push format_reset
	call printf					;			printf("%.2f" ANSI_COLOR_RESET "%s", array[k], k == size - 1 ? "\n" : "\t");
	add esp, 16
	pop ecx

	inc ecx
	jmp .for
.end:							;		}

;	/* optimization: partition size <= 3 already sorted by pivot selection */
	mov ecx, [hi]
	sub ecx, [lo]
	inc ecx
	cmp ecx, 3					;		if (hi-lo+1 <= 3)
	jle end_quick_sort			;			return;

;	/*
;	 * sort
;	 * optimization: 3 partition quicksort avoids poor performance with many duplicate elements
;	 */
	mov eax, [a]
	mov ebx, [lo]	; i
	mov ecx, [lo]	; j
	mov edx, [hi]	; n
sort_for:						;		for (i = j = lo, n = hi; j <= n;) {
	cmp ecx, edx
	jg .end

	mov dword [swap], -1		;			swap = swap2 = -1;
	mov dword [swap2], -1

.pivot.if:						;			if (a[j] < p) {
	fld dword [eax + ecx*4]
	fcomp dword [p]
	push eax
	fstsw ax
	sahf
	pop eax
	jnb .pivot.elif

; /* negligible performance effect. avoid unnecessary swap when i == j */
.pivot.if.if:					;				if (i != j) {
	cmp ebx, ecx
	je .pivot.if.end

	mov esi, [eax + ebx*4]		;					t = a[i];
	mov edi, [eax + ecx*4]
	mov [eax + ebx*4], edi		;					a[i] = a[j];
	mov [eax + ecx*4], esi		;					a[j] = t;

	mov [swap], ebx				;					swap = i;
	mov [swap2], ecx			;					swap2 = j;

.pivot.if.if.if:				;					if (i == m) {
	cmp ebx, [m]
	jne .pivot.if.if.elif
	mov [m], ecx				;						m = j;
	jmp .pivot.if.end
.pivot.if.if.elif:				;					} else if (j == m) {
	cmp ecx, [m]
	jne .pivot.if.end
	mov [m], ebx				;						m = i;
								;					}
.pivot.if.end:					;				}

	inc ebx						;				++i;
	inc ecx						;				++j;

	jmp .pivot.end
.pivot.elif:					;			} else if (a[j] > p) {
; no need to compare. use flags from first comparison
	jna .pivot.else

	mov esi, [eax + ecx*4]		;				t = a[j];
	mov edi, [eax + edx*4]
	mov [eax + ecx*4], edi		;				a[j] = a[n];
	mov [eax + edx*4], esi		;				a[n] = t;

	mov [swap], ecx				;				swap = j;
	mov [swap2], edx			;				swap2 = n;
.pivot.elif.if:					;				if (j == m) {
	cmp ecx, [m]
	jne .pivot.elif.elif

	mov [m], edx				;					m = n;
	jmp .pivot.elif.end
.pivot.elif.elif:				;				} else if (n == m) {
	cmp edx, [m]
	jne .pivot.elif.end

	mov [m], ecx				;					m = j;
.pivot.elif.end:				;				}

	dec edx						;				--n;
	jmp .pivot.end
.pivot.else:					;			} else {
	inc ecx						;				++j;
.pivot.end:						;			}

; print state
	pusha	; leave my registers alone
	mov ecx, 0
.state:							;			for (k = 0; k < size; ++k) {
	cmp ecx, [size]
	jnl .state.end

	cmp ecx, [lo]				;				if (k >= lo && k <= hi) {
	jl .state.if.end
	cmp ecx, [hi]
	jg .state.if.end

	cmp ecx, [m]				;					if (k == m) {
	jne .state.pivot.end

; push it, push it
	push edx
	push ecx
	push ANSI_COLOR_PIVOT
	call printf					;						printf(ANSI_COLOR_PIVOT);
	add esp, 4
	pop ecx
	pop edx
.state.pivot.end:				;					}

.state.if.if:					;					if (k == swap || k == swap2) {
	cmp ecx, [swap]
	je .state.if.if.condition
	cmp ecx, [swap2]
	jne .state.if.elif
.state.if.if.condition:

; to the limit, limit
	push edx
	push ecx
	push ANSI_COLOR_SWAP
	call printf					;						printf(ANSI_COLOR_SWAP);
	add esp, 4
	pop ecx
	pop edx

	jmp .state.if.end
.state.if.elif:					;					} else if (k < i || k > n) {
	cmp ecx, ebx
	jl .state.if.elif.condition
	cmp ecx, edx
	jng .state.if.else
.state.if.elif.condition:

; 'cause we're in it to win it
	push edx
	push ecx
	push ANSI_COLOR_REVERSE
	call printf					;						printf(ANSI_COLOR_REVERSE);
	add esp, 4
	pop ecx
	pop edx

	jmp .state.if.end
.state.if.else:					;					} else {
; in it to win it
	push edx
	push ecx
	push ANSI_COLOR_MID
	call printf					;						printf(ANSI_COLOR_MID);
	add esp, 4
	pop ecx
	pop edx
								;					}
.state.if.end:					;				}
; oh yeah
	push ebx
	push edx
	push ecx

	mov ebx, [size]
	dec ebx
	cmp ecx, ebx
	je .last
	push format_tab
	jmp .last.end
.last:
	push format_nl
.last.end:

	sub esp, 8
	mov eax, [a]
	fld dword [eax + ecx*4]
	fstp qword [esp]

	push format_reset
	call printf					;				printf("%.2f" ANSI_COLOR_RESET "%s", array[k], k == size - 1 ? "\n" : "\t");
	add esp, 16

	pop ecx
	pop edx
	pop ebx

	inc ecx
	jmp .state
.state.end:						;			}
	popa

	jmp sort_for
.end:							;		}

;	/* sort left and right partitions */
	push edx	; preserve for second call
	push eax

	dec ebx
	push ebx
	push dword [lo]
	push eax
	call quick_sort				;		quick_sort(a, lo, i-1);
	add esp, 12

	pop eax
	pop edx

	pusha
	push dword [hi]
	inc edx
	push edx
	push eax
	call quick_sort				;		quick_sort(a, n+1, hi);
	add esp, 12

end_quick_sort:
	pop ebx
	mov esp, ebp	;	}
	pop ebp
	ret

segment .data
	here db "HERE",10,0
debug:
	pusha
	push here
	call printf
	add esp, 4
	popa
	ret
