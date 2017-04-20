; CS131 MP1 - Mastermind code-breaking game
; Hope Ibelgaufts

%include "asm_io.inc"

segment .data
	; 9 = tab
	; 10 = newline
	menu db "Welcome to Mastermind",10,10,"[1] Play",10,"[2] Instructions",10,"[3] Quit",10,10,0
	prompt db "Enter selection: ",0

	code_prompt db "Codemaker, enter a code: ",0
	code_format db "Error: code must be four digits long and made up of digits from 1 to 6",10,0
	guess_start db "Codebreaker, begin guessing",10,10,0
	turn db "Turn ",0
	guess_prompt db "Guess: ",0
	correct_both db "Correct digits in correct position: ",0
	correct_digit db "Correct digits in wrong position:   ",0
	win_msg db "Code found. Codebreaker wins",10,10,0
	lose_msg db "Max turn reached. Codemaker wins",10,0
	reveal db "The code was ",0

	instruct1 db 10,"How to Play",10,10,"Mastermind is a game for two players. One player becomes the codemaker, the",10,0
	instruct2 db "other the codebreaker. The codemaker chooses a four digit code made up of",10,0
	instruct3 db "digits from 1 to 6. Repetition of digits is allowed. The codebreaker",10,0
	instruct4 db "tries to guess the code within twelve turns. After each guess the codebreaker",10,0
	instruct5 db "is told the number of correct digits that are in the right position as well",10,0
	instruct6 db "as the number of digits that are correct but in the wrong position.",10,10,0

	quit db "Thanks for playing",10,0

	; 80 newlines to hide code from codebreaker
	clear db 10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,10,0
segment .bss
	; code digits
	code1 resb 1
	code2 resb 1
	code3 resb 1
	code4 resb 1
	full_code resw 1

	; guess digits
	guess1 resb 1
	guess2 resb 1
	guess3 resb 1
	guess4 resb 1

segment .text
	global asm_main

asm_main:
	enter 0, 0
	pusha

begin:
	; show menu
	mov	eax, menu
	call print_string

	; get menu selection
select:
	mov eax, prompt
	call print_string
	call read_int

	; jump to program section
	cmp eax, 1
	je play

	cmp eax, 2
	je instructions

	cmp eax, 3
	je end

	; repeat prompt on bad input
	jmp select

play:
	; prompt for code
	mov eax, code_prompt
	call print_string
	call read_int
	mov ecx, eax

	; break code into digits. make sure they're digits from 1 to 6
	xor edx, edx
	mov ebx, 1000
	div ebx
	cmp eax, 1
	jl bad_code
	cmp eax, 6
	jg bad_code
	mov [code1], eax

	mov eax, edx
	xor edx, edx
	mov ebx, 100
	div ebx
	cmp eax, 1
	jl bad_code
	cmp eax, 6
	jg bad_code
	mov [code2], eax

	mov eax, edx
	xor edx, edx
	mov ebx, 10
	div ebx
	cmp eax, 1
	jl bad_code
	cmp eax, 6
	jg bad_code
	mov [code3], eax

	cmp edx, 1
	jl bad_code
	cmp edx, 6
	jg bad_code
	mov [code4], edx

	mov [full_code], cx ; code is valid, meaning it fits in two bytes

	jmp end_bad_code

bad_code:
	mov eax, code_format
	call print_string
	jmp play
end_bad_code:

	; clear screen and begin guessing
	mov eax, clear
	call print_string
	mov eax, guess_start
	call print_string

	xor ecx, ecx ; zero out cl and ch
	inc cl ; start at 1 so we can print turn number easily
guess:
	cmp cl, 12
	jg lose

	mov eax, turn
	call print_string
	movzx eax, cl ; shorter than setting to zero then copying to al. movzx is in chapter 2 of the book
	call print_int
	call print_nl
	mov eax, guess_prompt
	call print_string
	call read_int

	; break guess into digits. make sure they're digits from 1 to 6
	xor edx, edx
	mov ebx, 1000
	div ebx
	cmp eax, 1
	jl bad_guess
	cmp eax, 6
	jg bad_guess
	mov [guess1], eax

	mov eax, edx
	xor edx, edx
	mov ebx, 100
	div ebx
	cmp eax, 1
	jl bad_guess
	cmp eax, 6
	jg bad_guess
	mov [guess2], eax

	mov eax, edx
	xor edx, edx
	mov ebx, 10
	div ebx
	cmp eax, 1
	jl bad_guess
	cmp eax, 6
	jg bad_guess
	mov [guess3], eax

	cmp edx, 1
	jl bad_guess
	cmp edx, 6
	jg bad_guess
	mov [guess4], edx

	jmp end_bad_guess

bad_guess:
	mov eax, code_format
	call print_string
	jmp guess
end_bad_guess:

	; check guess

	xor eax, eax ; just to be paranoid
	xor ebx, ebx

	; count correct digit and position. store in bl
	mov al, [guess1]
	cmp al, [code1]
	jne first_ne ; i'd prefer setz but it hasn't been taught yet so instead you get labels galore
	inc bl
first_ne:
	mov al, [guess2]
	cmp al, [code2]
	jne second_ne
	inc bl
second_ne:
	mov al, [guess3]
	cmp al, [code3]
	jne third_ne
	inc bl
third_ne:
	mov al, [guess4]
	cmp al, [code4]
	jne fourth_ne
	inc bl
fourth_ne:

	; count correct digit ignoring position. store in bh
	mov ch, 1
for_digit:
	cmp ch, 6
	jg end_for_digit

	xor eax, eax ; can't believe i forgot this before

	; count occurrences of ch in code and guess and store in al and ah respectively
	; occurrences in code
	cmp ch, [code1]
	jne code1_ne
	inc al
code1_ne:
	cmp ch, [code2]
	jne code2_ne
	inc al
code2_ne:
	cmp ch, [code3]
	jne code3_ne
	inc al
code3_ne:
	cmp ch, [code4]
	jne code4_ne
	inc al
code4_ne:

	; occurrences in guess
	cmp ch, [guess1]
	jne guess1_ne
	inc ah
guess1_ne:
	cmp ch, [guess2]
	jne guess2_ne
	inc ah
guess2_ne:
	cmp ch, [guess3]
	jne guess3_ne
	inc ah
guess3_ne:
	cmp ch, [guess4]
	jne guess4_ne
	inc ah
guess4_ne:

	; add smaller value to bh
	cmp al, ah
	jl al_min
	mov al, ah ; to avoid multiple jumps
al_min:
	add bh, al

	inc ch
	jmp for_digit
end_for_digit:

	; correct digits ignoring position = correct position + wrong position
	; correct digits ignoring position = bh
	; correct digits in wrong position = bh - bl
	; correct digits in correct position = bl

	cmp bl, 4 ; all digits correct and in correct position
	je win

	mov	eax, correct_both
	call print_string
	movzx eax, bl ; extend bl to 32 bits
	call print_int
	call print_nl

	mov eax, correct_digit
	call print_string
	mov al, bh
	sub al, bl
	movzx eax, al ; TODO: unnecessary?
	call print_int
	call print_nl
	call print_nl

	inc cl ; increment turn counter
	jmp guess

win:
	mov eax, win_msg
	call print_string
	jmp begin

lose:
	mov eax, lose_msg
	call print_string
	mov eax, reveal
	call print_string
	mov ax, [full_code]
	movzx eax, ax ; TODO: unnecessary?
	call print_int
	call print_nl
	call print_nl
	jmp begin

instructions:
	mov eax, instruct1
	call print_string
	mov eax, instruct2
	call print_string
	mov eax, instruct3
	call print_string
	mov eax, instruct4
	call print_string
	mov eax, instruct5
	call print_string
	mov eax, instruct6
	call print_string

	jmp begin

end:
	mov eax, quit
	call print_string

	popa
	mov eax, 0
	leave
	ret
