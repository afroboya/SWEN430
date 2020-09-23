
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
	movq -8(%rbp), %rbx
	movq %rbx, 8(%rax)
	movq -16(%rbp), %rax
	movq $16, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $1, %rcx
	movq %rcx, 0(%rbx)
	movq $96, %rcx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, 8(%rsp)
	movq %rcx, %rdi
	call malloc
	movq %rax, %rcx
	movq 0(%rsp), %rax
	movq 8(%rsp), %rbx
	addq $16, %rsp
	movq $11, %rdx
	movq %rdx, 0(%rcx)
	movq %rcx, 8(%rbx)
	movq $72, %rdx
	movq %rdx, 8(%rcx)
	movq $101, %rdx
	movq %rdx, 16(%rcx)
	movq $108, %rdx
	movq %rdx, 24(%rcx)
	movq $108, %rdx
	movq %rdx, 32(%rcx)
	movq $111, %rdx
	movq %rdx, 40(%rcx)
	movq $32, %rdx
	movq %rdx, 48(%rcx)
	movq $87, %rdx
	movq %rdx, 56(%rcx)
	movq $111, %rdx
	movq %rdx, 64(%rcx)
	movq $114, %rdx
	movq %rdx, 72(%rcx)
	movq $108, %rdx
	movq %rdx, 80(%rcx)
	movq $100, %rdx
	movq %rdx, 88(%rcx)
	jmp label422
	movq $1, %rax
	jmp label423
label422:
	movq $0, %rax
label423:
	movq %rax, %rdi
	call assertion
	movq -16(%rbp), %rax
	movq $16, %rbx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, %rdi
	call malloc
	movq %rax, %rbx
	movq 0(%rsp), %rax
	addq $16, %rsp
	movq $1, %rcx
	movq %rcx, 0(%rbx)
	movq $40, %rcx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, 8(%rsp)
	movq %rcx, %rdi
	call malloc
	movq %rax, %rcx
	movq 0(%rsp), %rax
	movq 8(%rsp), %rbx
	addq $16, %rsp
	movq $4, %rdx
	movq %rdx, 0(%rcx)
	movq %rcx, 8(%rbx)
	movq $66, %rdx
	movq %rdx, 8(%rcx)
	movq $108, %rdx
	movq %rdx, 16(%rcx)
	movq $97, %rdx
	movq %rdx, 24(%rcx)
	movq $104, %rdx
	movq %rdx, 32(%rcx)
	movq $1, %rax
	jmp label425
label424:
	movq $0, %rax
label425:
	movq %rax, %rdi
	call assertion
label421:
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
