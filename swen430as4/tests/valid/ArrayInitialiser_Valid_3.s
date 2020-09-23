
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq %rax, 16(%rbp)
	jmp label506
label506:
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
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label508
	movq $1, %rax
	jmp label509
label508:
	movq $0, %rax
label509:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label510
	movq $1, %rax
	jmp label511
label510:
	movq $0, %rax
label511:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $2, %rbx
	cmpq %rax, %rbx
	jnz label512
	movq $1, %rax
	jmp label513
label512:
	movq $0, %rax
label513:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $3, %rbx
	cmpq %rax, %rbx
	jnz label514
	movq $1, %rax
	jmp label515
label514:
	movq $0, %rax
label515:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $4, %rbx
	cmpq %rax, %rbx
	jnz label516
	movq $1, %rax
	jmp label517
label516:
	movq $0, %rax
label517:
	movq %rax, %rdi
	call assertion
label507:
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
