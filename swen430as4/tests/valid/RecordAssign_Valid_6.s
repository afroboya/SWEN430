
	.text
wl_f:
	pushq %rbp
	movq %rsp, %rbp
	movq 24(%rbp), %rax
	movq 8(%rax), %rbx
	movq $1, %rax
	movq %rax, 8(%rbx)
	movq 24(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label402
label402:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $16, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $1, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -16(%rbp)
	movq $0, %rbx
	movq %rbx, 8(%rax)
	movq $16, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $1, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq -16(%rbp), %rbx
	movq %rbx, 8(%rax)
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq -16(%rbp), %rbx
	jmp label404
	movq $1, %rax
	jmp label405
label404:
	movq $0, %rax
label405:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	movq -8(%rbp), %rax
	movq %rax, 8(%rsp)
	call wl_f
	addq $16, %rsp
	movq -16(%rsp), %rax
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
	movq $16, %rcx
	subq $16, %rsp
	movq %rax, 0(%rsp)
	movq %rbx, 8(%rsp)
	movq %rcx, %rdi
	call malloc
	movq %rax, %rcx
	movq 0(%rsp), %rax
	movq 8(%rsp), %rbx
	addq $16, %rsp
	movq $1, %rdx
	movq %rdx, 0(%rcx)
	movq %rcx, 8(%rbx)
	movq $1, %rdx
	movq %rdx, 8(%rcx)
	jmp label406
	movq $1, %rax
	jmp label407
label406:
	movq $0, %rax
label407:
	movq %rax, %rdi
	call assertion
label403:
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
