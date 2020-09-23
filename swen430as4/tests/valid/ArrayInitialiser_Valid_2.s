
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq $2, %rbx
	cmpq %rbx, %rax
	jle label376
	movq $1, %rax
	movq %rax, 16(%rbp)
	jmp label374
	jmp label375
label376:
	movq $0, %rax
	movq %rax, 16(%rbp)
	jmp label374
label375:
label374:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label378
	movq $1, %rax
	jmp label379
label378:
	movq $0, %rax
label379:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label380
	movq $1, %rax
	jmp label381
label380:
	movq $0, %rax
label381:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label382
	movq $1, %rax
	jmp label383
label382:
	movq $0, %rax
label383:
	movq %rax, %rdi
	call assertion
label377:
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
