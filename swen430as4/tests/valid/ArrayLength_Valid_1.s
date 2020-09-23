
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label519
	movq $1, %rax
	jmp label520
label519:
	movq $0, %rax
label520:
	movq %rax, %rdi
	call assertion
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label521
	movq $1, %rax
	jmp label522
label521:
	movq $0, %rax
label522:
	movq %rax, %rdi
	call assertion
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label523
	movq $1, %rax
	jmp label524
label523:
	movq $0, %rax
label524:
	movq %rax, %rdi
	call assertion
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label525
	movq $1, %rax
	jmp label526
label525:
	movq $0, %rax
label526:
	movq %rax, %rdi
	call assertion
label518:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
