
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
	movq $16, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $1, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -16(%rbp)
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
	movq %rbx, 8(%rax)
	movq $98, %rcx
	movq %rcx, 8(%rbx)
	movq $108, %rcx
	movq %rcx, 16(%rbx)
	movq $97, %rcx
	movq %rcx, 24(%rbx)
	movq $104, %rcx
	movq %rcx, 32(%rbx)
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq -8(%rbp), %rbx
	movq $1, %rax
	jmp label412
label411:
	movq $0, %rax
label412:
	movq %rax, %rdi
	call assertion
	movq -16(%rbp), %rax
	movq -8(%rbp), %rbx
	movq %rbx, 8(%rax)
	movq -16(%rbp), %rax
	movq 8(%rax), %rax
	movq -8(%rbp), %rbx
	jmp label413
	movq $1, %rax
	jmp label414
label413:
	movq $0, %rax
label414:
	movq %rax, %rdi
	call assertion
label410:
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
