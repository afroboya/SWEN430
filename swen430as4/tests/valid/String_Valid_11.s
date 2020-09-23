
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
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
	movq $48, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $5, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -16(%rbp)
	movq $87, %rbx
	movq %rbx, 8(%rax)
	movq $111, %rbx
	movq %rbx, 16(%rax)
	movq $114, %rbx
	movq %rbx, 24(%rax)
	movq $108, %rbx
	movq %rbx, 32(%rax)
	movq $100, %rbx
	movq %rbx, 40(%rax)
	movq -8(%rbp), %rax
	movq -16(%rbp), %rbx
	movq $1, %rax
	jmp label666
label665:
	movq $0, %rax
label666:
	movq %rax, %rdi
	call assertion
label664:
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
