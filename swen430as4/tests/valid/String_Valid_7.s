
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 24(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label677
label677:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $96, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $11, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq $72, %rbx
	movq %rbx, 8(%rax)
	movq $101, %rbx
	movq %rbx, 16(%rax)
	movq $108, %rbx
	movq %rbx, 24(%rax)
	movq $108, %rbx
	movq %rbx, 32(%rax)
	movq $111, %rbx
	movq %rbx, 40(%rax)
	movq $32, %rbx
	movq %rbx, 48(%rax)
	movq $87, %rbx
	movq %rbx, 56(%rax)
	movq $111, %rbx
	movq %rbx, 64(%rax)
	movq $114, %rbx
	movq %rbx, 72(%rax)
	movq $108, %rbx
	movq %rbx, 80(%rax)
	movq $100, %rbx
	movq %rbx, 88(%rax)
	subq $16, %rsp
	movq -8(%rbp), %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq -8(%rbp), %rbx
	jmp label679
	movq $1, %rax
	jmp label680
label679:
	movq $0, %rax
label680:
	movq %rax, %rdi
	call assertion
label678:
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
