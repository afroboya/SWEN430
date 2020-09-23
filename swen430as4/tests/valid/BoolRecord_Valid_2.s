
	.text
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $24, %rax
	movq %rax, %rdi
	call malloc
	movq %rax, %rax
	movq $2, %rbx
	movq %rbx, 0(%rax)
	movq %rax, -8(%rbp)
	movq $1, %rbx
	movq %rbx, 8(%rax)
	movq $0, %rbx
	movq %rbx, 16(%rax)
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	cmpq $0, %rax
	jz label572
	movq $1, %rax
	movq %rax, %rdi
	call assertion
	jmp label571
label572:
	movq $0, %rax
	movq %rax, %rdi
	call assertion
label571:
label570:
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
