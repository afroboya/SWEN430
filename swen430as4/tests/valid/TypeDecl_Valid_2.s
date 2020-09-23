
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
	movq $0, %rbx
	movq %rbx, 8(%rax)
	movq $1, %rbx
	movq %rbx, 16(%rax)
	movq -8(%rbp), %rax
	movq 8(%rax), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label725
	movq $1, %rax
	jmp label726
label725:
	movq $0, %rax
label726:
	movq %rax, %rdi
	call assertion
	movq -8(%rbp), %rax
	movq 16(%rax), %rax
	movq $1, %rbx
	cmpq %rax, %rbx
	jnz label727
	movq $1, %rax
	jmp label728
label727:
	movq $0, %rax
label728:
	movq %rax, %rdi
	call assertion
label724:
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
