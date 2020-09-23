
	.text
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
	movq -8(%rbp), %rax
	movq $96, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $11, %rcx
	movq %rcx, 0(%rbx)
	movq $72, %rcx
	movq %rcx, 8(%rbx)
	movq $101, %rcx
	movq %rcx, 16(%rbx)
	movq $108, %rcx
	movq %rcx, 24(%rbx)
	movq $108, %rcx
	movq %rcx, 32(%rbx)
	movq $111, %rcx
	movq %rcx, 40(%rbx)
	movq $32, %rcx
	movq %rcx, 48(%rbx)
	movq $87, %rcx
	movq %rcx, 56(%rbx)
	movq $111, %rcx
	movq %rcx, 64(%rbx)
	movq $114, %rcx
	movq %rcx, 72(%rbx)
	movq $108, %rcx
	movq %rcx, 80(%rbx)
	movq $100, %rcx
	movq %rcx, 88(%rbx)
	jmp label672
	movq $1, %rax
	jmp label673
label672:
	movq $0, %rax
label673:
	movq %rax, %rdi
	call assertion
label671:
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
