
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 24(%rbp), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label551
	movq $40, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $4, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $84, %rbx
	movq %rbx, 8(%rax)
	movq $82, %rbx
	movq %rbx, 16(%rax)
	movq $85, %rbx
	movq %rbx, 24(%rax)
	movq $69, %rbx
	movq %rbx, 32(%rax)
	jmp label549
	jmp label550
label551:
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
	movq %rbx, 0(%rax)
	movq %rax, 16(%rbp)
	movq $70, %rbx
	movq %rbx, 8(%rax)
	movq $65, %rbx
	movq %rbx, 16(%rax)
	movq $76, %rbx
	movq %rbx, 24(%rax)
	movq $83, %rbx
	movq %rbx, 32(%rax)
	movq $69, %rbx
	movq %rbx, 40(%rax)
	jmp label549
label550:
label549:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	movq $40, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $4, %rbx
	movq %rbx, 0(%rax)
	movq $84, %rbx
	movq %rbx, 8(%rax)
	movq $82, %rbx
	movq %rbx, 16(%rax)
	movq $85, %rbx
	movq %rbx, 24(%rax)
	movq $69, %rbx
	movq %rbx, 32(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $1, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label553
	movq $1, %rax
	jmp label554
label553:
	movq $0, %rax
label554:
	movq %rax, %rdi
	call assertion
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
	movq %rbx, 0(%rax)
	movq $70, %rbx
	movq %rbx, 8(%rax)
	movq $65, %rbx
	movq %rbx, 16(%rax)
	movq $76, %rbx
	movq %rbx, 24(%rax)
	movq $83, %rbx
	movq %rbx, 32(%rax)
	movq $69, %rbx
	movq %rbx, 40(%rax)
	subq $16, %rsp
	movq %rax, 0(%rsp)
	subq $16, %rsp
	movq $0, %rbx
	movq %rbx, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq -32(%rsp), %rbx
	jmp label555
	movq $1, %rax
	jmp label556
label555:
	movq $0, %rax
label556:
	movq %rax, %rdi
	call assertion
label552:
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
