
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 40(%rbp), %rax
	movq 32(%rbp), %rbx
	movq %rax, %rax
	cqto
	idivq %rbx
	movq %rax, %rax
	movq 24(%rbp), %rbx
	movq %rax, %rax
	cqto
	idivq %rbx
	movq %rax, %rax
	movq %rax, 16(%rbp)
	jmp label311
label311:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq $3, %rax
	movq %rax, 8(%rsp)
	movq $4, %rax
	movq %rax, 16(%rsp)
	movq $100, %rax
	movq %rax, 24(%rsp)
	call wl_f
	addq $32, %rsp
	movq -32(%rsp), %rax
	movq $8, %rbx
	cmpq %rax, %rbx
	jnz label313
	movq $1, %rax
	jmp label314
label313:
	movq $0, %rax
label314:
	movq %rax, %rdi
	call assertion
label312:
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
