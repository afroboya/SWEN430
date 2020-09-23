
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 24(%rbp), %rax
	cmpq $0, %rax
	jz label543
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
	jmp label541
	jmp label542
label543:
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
	jmp label541
label542:
label541:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $1, %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $40, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $4, %rcx
	movq %rcx, 0(%rbx)
	movq $84, %rcx
	movq %rcx, 8(%rbx)
	movq $82, %rcx
	movq %rcx, 16(%rbx)
	movq $85, %rcx
	movq %rcx, 24(%rbx)
	movq $69, %rcx
	movq %rcx, 32(%rbx)
	jmp label545
	movq $1, %rax
	jmp label546
label545:
	movq $0, %rax
label546:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	movq $0, %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $48, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $5, %rcx
	movq %rcx, 0(%rbx)
	movq $70, %rcx
	movq %rcx, 8(%rbx)
	movq $65, %rcx
	movq %rcx, 16(%rbx)
	movq $76, %rcx
	movq %rcx, 24(%rbx)
	movq $83, %rcx
	movq %rcx, 32(%rbx)
	movq $69, %rcx
	movq %rcx, 40(%rbx)
	jmp label547
	movq $1, %rax
	jmp label548
label547:
	movq $0, %rax
label548:
	movq %rax, %rdi
	call assertion
label544:
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
