
	.text
wl_sum:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -8(%rbp)
	movq $0, %rax
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label733
label733:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label735
	movq $1, %rax
	jmp label736
label735:
	movq $0, %rax
label736:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $6, %rbx
	cmpq %rax, %rbx
	jnz label737
	movq $1, %rax
	jmp label738
label737:
	movq $0, %rax
label738:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $1108, %rbx
	cmpq %rax, %rbx
	jnz label739
	movq $1, %rax
	jmp label740
label739:
	movq $0, %rax
label740:
	movq %rax, %rdi
	call assertion
label734:
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
