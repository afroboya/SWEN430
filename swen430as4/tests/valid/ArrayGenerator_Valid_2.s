
	.text
wl_abs:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -16(%rbp)
	movq -8(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label484
label484:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	subq $16, %rsp
	call wl_abs
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	jmp label486
	movq $1, %rax
	jmp label487
label486:
	movq $0, %rax
label487:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_abs
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq %rax, -8(%rbp)
	movq -8(%rbp), %rax
	jmp label488
	movq $1, %rax
	jmp label489
label488:
	movq $0, %rax
label489:
	movq %rax, %rdi
	call assertion
label485:
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
